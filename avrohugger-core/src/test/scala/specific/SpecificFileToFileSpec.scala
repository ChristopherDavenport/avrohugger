package avrohugger
package test
package specific

import java.io.File

import avrohugger._
import avrohugger.format.SpecificRecord
import org.specs2._

class SpecificFileToFileSpec extends Specification {

  def is = s2"""
    SpecificRecord Generator fileToFiles method should
      correctly generate a simple case class definition from AVRO $eA
      not generate copy of imported classes in the importing package $e0
      correctly generate from a protocol with messages $e1
      correctly generate a simple case class definition in a package $e2
      correctly generate a simple case class definition in the default package $e3
      correctly generate a nested case class definition from a schema $e4
      correctly generate a nested case class from IDL $e5
      correctly generate a recursive case class from IDL $e6
      correctly generate enums from schema $e7
      correctly generate enums from protocol $e8
      correctly generate enums from IDL $e9
      correctly generate nested enums $e10
      correctly generate bytes from schema $e11
      correctly generate bytes from protocol $e12
      correctly generate bytes from IDL $e13
      correctly generate records depending on others defined in a different- and same-namespaced AVDL and AVSC $e14
      correctly generate an empty case class definition $e15
      correctly generate default values $e16
  """
  
  // tests specific to fileToX
  def eA = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/twitter.avro")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)
    
    val source = util.Util.readFile("target/generated-sources/specific/com/miguno/avro/twitter_schema.scala")
    
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/com/miguno/avro/twitter_schema.scala")
  }
  
  // tests specific to fileToFile
  def e0 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/import.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    (new File(s"target/generated-sources/specific/example/idl/ExternalDependency.scala")).exists === false
  }
  
  // tests common to fileToX and stringToX
  def e1 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/mail.avpr")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val sourceTrait = util.Util.readFile(s"$outDir/example/proto/Mail.scala")
    val sourceRecord = util.Util.readFile(s"$outDir/example/proto/Message.scala")

    sourceTrait === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/proto/Mail.scala")
    sourceRecord === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/proto/Message.scala")
  }
  
  def e2 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/user.avsc")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)
    
    val source = util.Util.readFile("target/generated-sources/specific/example/User.scala")
    
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/User.scala")
  }
  
  def e3 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/AvroTypeProviderTestNoNamespace.avsc")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)
    
    val source = util.Util.readFile("target/generated-sources/specific/AvroTypeProviderTestNoNamespace.scala")
    
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/AvroTypeProviderTestNoNamespace.scala")
  }
  
  def e4 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/nested.avsc")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val source0 = util.Util.readFile("target/generated-sources/specific/example/Level0.scala")
    val source1 = util.Util.readFile("target/generated-sources/specific/example/Level1.scala")
    val source2 = util.Util.readFile("target/generated-sources/specific/example/Level2.scala")

    source0 === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/Level0.scala")
    source1 === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/Level1.scala")
    source2 === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/Level2.scala")
  }
  
  def e5 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/nested.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)
    val source = util.Util.readFile("target/generated-sources/specific/example/idl/NestedProtocol.scala")
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/NestedProtocol.scala")
  }
  
  def e6 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/recursive.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val source = util.Util.readFile("target/generated-sources/specific/example/idl/Recursive.scala")
    
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/Recursive.scala")
  }
  
  def e7 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/enums.avsc")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val source = util.Util.readFile("target/generated-sources/specific/example/Suit.java")
    
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/Suit.java")
  }
  
  def e8 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/enums.avpr")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val sourceEnum = util.Util.readFile("target/generated-sources/specific/example/proto/Suit.java")
    val sourceRecord = util.Util.readFile("target/generated-sources/specific/example/proto/Card.scala")
    
    sourceEnum === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/proto/Suit.java")
    sourceRecord === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/proto/Card.scala")
  }
  
  def e9 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/enums.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val sourceEnum = util.Util.readFile("target/generated-sources/specific/example/idl/Suit.java")
    val sourceRecord = util.Util.readFile("target/generated-sources/specific/example/idl/Card.scala")

    sourceEnum === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/Suit.java")
    sourceRecord === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/Card.scala")
  }
  
  def e10 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/enums_nested.avsc")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)
    
    val sourceEnum = util.Util.readFile("target/generated-sources/specific/example/Direction.java")
    val sourceRecord = util.Util.readFile("target/generated-sources/specific/example/Compass.scala")
    
    sourceEnum === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/Direction.java")
    sourceRecord === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/Compass.scala")
  }
  
  def e11 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/bytes.avsc")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val source = util.Util.readFile("target/generated-sources/specific/example/BinarySc.scala")
    
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/BinarySc.scala")
  }
  
  def e12 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/bytes.avpr")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val source = util.Util.readFile("target/generated-sources/specific/example/proto/BinaryPr.scala")

    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/proto/BinaryPr.scala")
  }
  
  def e13 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/bytes.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val source = util.Util.readFile("target/generated-sources/specific/example/idl/BinaryIdl.scala")
  
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/BinaryIdl.scala")
  }
  
  def e14 = {
    val importing = new java.io.File("avrohugger-core/src/test/avro/import.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(importing, outDir)

    val sourceADT = util.Util.readFile("target/generated-sources/specific/example/idl/ImportProtocol.scala")
    val sourceDep1 = util.Util.readFile("target/generated-sources/specific/example/idl/Defaults.scala")
    val sourceEnum = util.Util.readFile("target/generated-sources/specific/example/idl/DefaultEnum.java")
    val sourceDep2 = util.Util.readFile("target/generated-sources/specific/other/ns/ExternalDependency.scala")
    val sourceDep3 = util.Util.readFile("target/generated-sources/specific/other/ns/Suit.java")
    
    sourceADT === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/ImportProtocol.scala")
    sourceDep1 === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/Defaults.scala")
    sourceEnum === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/DefaultEnum.java")
    sourceDep2 === util.Util.readFile("avrohugger-core/src/test/expected/specific/other/ns/ExternalDependency.scala")
    sourceDep3 === util.Util.readFile("avrohugger-core/src/test/expected/specific/other/ns/Suit.java")
  }
  
  def e15 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/AvroTypeProviderTestEmptyRecord.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)
  
    val source = util.Util.readFile("target/generated-sources/specific/test/Calculator.scala")
  
    source === util.Util.readFile("avrohugger-core/src/test/expected/specific/test/Calculator.scala")
  }
  
  def e16 = {
    val infile = new java.io.File("avrohugger-core/src/test/avro/defaults.avdl")
    val gen = new Generator(SpecificRecord)
    val outDir = gen.defaultOutputDir + "/specific/"
    gen.fileToFile(infile, outDir)

    val sourceRecord = util.Util.readFile("target/generated-sources/specific/example/idl/Defaults.scala")
    val sourceEnum = util.Util.readFile("target/generated-sources/specific/example/idl/DefaultEnum.java")

    sourceRecord === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/Defaults.scala")
    sourceEnum === util.Util.readFile("avrohugger-core/src/test/expected/specific/example/idl/DefaultEnum.java")
  }

}
