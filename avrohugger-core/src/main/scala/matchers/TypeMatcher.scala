package avrohugger
package matchers

import stores.ClassStore
import treehugger.forest._
import treehuggerDSL._
import definitions._
import org.apache.avro.Schema
import org.apache.avro.Schema.{Type => AvroType}
import java.util.concurrent.ConcurrentHashMap

import treehugger.forest

import scala.collection.convert.Wrappers.JConcurrentMapWrapper
import scala.collection.JavaConverters._

class TypeMatcher {

  // holds user-defined custom type mappings, e.g. ("array"->classOf[Array[_]])
  val customTypeMap: scala.collection.concurrent.Map[String, Class[_]] = {
    JConcurrentMapWrapper(new ConcurrentHashMap[String, Class[_]]())
  }
  
  // holds user-defined custom namespace mappings, 
  // e.g. ("com.example.idl"->"com.example.model")
  val customNamespaceMap: scala.collection.concurrent.Map[String, String] = {
    JConcurrentMapWrapper(new ConcurrentHashMap[String, String]())
  }
  
  // holds user-selected enum style, e.g. ("enum"->"java_enum")
  val customEnumStyleMap: scala.collection.concurrent.Map[String, String] = {
    JConcurrentMapWrapper(new ConcurrentHashMap[String, String]())
  }
  

  // updates the type map to allow for custom avro to scala mappings
  def updateCustomTypeMap(avroToScalaMapEntry: (String, Class[_])) {
    val _ = customTypeMap += avroToScalaMapEntry
  }

  // updates the namespace map to allow for custom avro to scala mappings
  def updateCustomNamespaceMap(customNamespaceMapEntry: (String, String)) {
    val _ = customNamespaceMap += customNamespaceMapEntry
  }
  
  // updates the enum style map map to allow for avro to java or scala mappings
  def updateCustomEnumStyleMap(customEnumStyleMapEntry: (String, String)) {
    val _ = customEnumStyleMap += customEnumStyleMapEntry
  }

  def toScalaType(
    classStore: ClassStore,
    namespace: Option[String],
    schema: Schema): Type = {
    // May contain nested schemas that will use the same namespace as the
    // top-level schema. Thus, when a field is parsed, the namespace is passed.
    def matchType(schema: Schema): Type = {

      schema.getType match {
        case Schema.Type.ARRAY    => {
          // default array mapping is currently List, for historical reasons
          val avroElement = schema.getElementType
          val scalaElementType = toScalaType(classStore, namespace, avroElement)
          val collectionType = CustomTypeMatcher.checkCustomArrayType(
            customTypeMap.get("array"),
            TYPE_LIST)
          collectionType(scalaElementType)
        }
        case Schema.Type.MAP      => {
          val keyType = StringClass
          val avroValueType = schema.getValueType
          val scalaValueType = toScalaType(classStore, namespace, avroValueType)
          TYPE_MAP(keyType, scalaValueType)
        }
        case Schema.Type.BOOLEAN  => BooleanClass
        case Schema.Type.DOUBLE   => CustomTypeMatcher.checkCustomNumberType(
          customTypeMap.get("double"), DoubleClass)
        case Schema.Type.FLOAT    => CustomTypeMatcher.checkCustomNumberType(
          customTypeMap.get("float"), FloatClass)
        case Schema.Type.LONG     => CustomTypeMatcher.checkCustomNumberType(
          customTypeMap.get("long"), LongClass)
        case Schema.Type.INT      => CustomTypeMatcher.checkCustomNumberType(
          customTypeMap.get("int"), IntClass)
        case Schema.Type.NULL     => NullClass
        case Schema.Type.STRING   => StringClass
        case Schema.Type.FIXED    => sys.error("FIXED datatype not supported")
        case Schema.Type.BYTES    => TYPE_ARRAY(ByteClass)
        case Schema.Type.RECORD   => classStore.generatedClasses(schema)
        case Schema.Type.ENUM     => classStore.generatedClasses(schema)
        case Schema.Type.UNION    => {
          //unions are represented as shapeless.Coproduct
          val unionSchemas = schema.getTypes.asScala.toList
          unionTypeImpl(unionSchemas, matchType)
        }
        case x => sys.error( x + " is not supported or not a valid Avro type")
      }
    }
    
    matchType(schema)
  }

  /**
    * Handles unions with the following type translations
    *
    * union:null,T => Option[T]
    * union:L,R => Either[L, R]
    * union:A,B,C => A :+: B :+: C :+: CNil
    * union:null,L,R => Option[Either[L, R]]
    * union:null,A,B,C => Option[A :+: B :+: C :+: CNil]
    *
    * If a null is found at any position in the union the entire type is wrapped in Option and null removed from the
    * types. Per the avro spec which is ambiguous about this:
    *
    * https://avro.apache.org/docs/1.8.1/spec.html#Unions
    *
    * (Note that when a default value is specified for a record field whose type is a union, the type of the default
    * value must match the first element of the union. Thus, for unions containing "null", the "null" is usually listed
    * first, since the default value of such unions is typically null.)
    */
  private[this] def unionTypeImpl(unionSchemas: List[Schema], typeMatcher: (Schema) => Type) : Type = {

    def shapelessCoproductType(tp: Type*): forest.Type =  {
      val copTypes = tp.toList :+ typeRef(RootClass.newClass(newTypeName("CNil")))
      val chain: forest.Tree = INFIX_CHAIN(":+:", copTypes.map(t => Ident(t.safeToString)))
      val chainedS = treeToString(chain)
      typeRef(RootClass.newClass(newTypeName(chainedS)))
    }

    val includesNull: Boolean = unionSchemas.exists(_.getType == Schema.Type.NULL)

    val nonNullableSchemas: List[Schema] = unionSchemas.filter(_.getType != Schema.Type.NULL)

    val matchedType = nonNullableSchemas match {
      case List(schemaA) =>
        typeMatcher(schemaA)
      case List(schemaA, schemaB) =>
        eitherType(typeMatcher(schemaA), typeMatcher(schemaB))
      case _ =>
        shapelessCoproductType(nonNullableSchemas.map(typeMatcher): _*)
    }

    if (includesNull) optionType(matchedType) else matchedType
  }


  //Scavro requires Java types be generated for mapping Java classes to Scala

  // in the future, scavro may allow this to be set
  val avroStringType = TYPE_REF("CharSequence") 

  def toJavaType(
    classStore: ClassStore,
    namespace: Option[String],
    schema: Schema): Type = {
    // The schema may contain nested schemas that will use the same namespace 
    // as the top-level schema.  Thus, when a field is parsed, the namespace is 
    // passed in once
    def matchType(schema: Schema): Type = {
      def javaRename(schema: Schema) = {
        "J" + classStore.generatedClasses(schema)
      }

      schema.getType match { 
        case Schema.Type.INT => TYPE_REF("java.lang.Integer")
        case Schema.Type.DOUBLE => TYPE_REF("java.lang.Double")
        case Schema.Type.FLOAT => TYPE_REF("java.lang.Float")
        case Schema.Type.LONG => TYPE_REF("java.lang.Long")
        case Schema.Type.BOOLEAN => TYPE_REF("java.lang.Boolean")
        case Schema.Type.STRING => avroStringType
        case Schema.Type.ARRAY => {
          val avroElement = schema.getElementType
          val elementType = toJavaType(classStore, namespace, avroElement)
          TYPE_REF(REF("java.util.List") APPLYTYPE(elementType))
        }
        case Schema.Type.MAP      => {
          val keyType = avroStringType
          val valueType = toJavaType(classStore, namespace, schema.getValueType)
          TYPE_REF(REF("java.util.Map") APPLYTYPE(keyType, valueType))
        }
        case Schema.Type.NULL     => TYPE_REF("java.lang.Void")
        case Schema.Type.FIXED    => sys.error("FIXED datatype not supported")
        case Schema.Type.BYTES    => TYPE_REF("java.nio.ByteBuffer")
        case Schema.Type.RECORD   => TYPE_REF(javaRename(schema))
        case Schema.Type.ENUM     => TYPE_REF(javaRename(schema))
        case Schema.Type.UNION    => { 
          val unionSchemas = schema.getTypes.asScala.toList
          // unions are represented as Scala Option[T], and thus unions must be 
          // of two types, one of them NULL
          val isTwoTypes = unionSchemas.length == 2
          val oneTypeIsNull = unionSchemas.exists(_.getType == Schema.Type.NULL)
          if (isTwoTypes && oneTypeIsNull) {
            val maybeSchema = unionSchemas.find(_.getType != Schema.Type.NULL)
            if (maybeSchema.isDefined ) matchType(maybeSchema.get)
            else sys.error("no avro type found in this union")  
          }
          else sys.error("unions not yet supported beyond nullable fields")
        }
        case x => sys.error( x +  " is not supported or not a valid Avro type")
      }
    }

    matchType(schema)
  }
}
