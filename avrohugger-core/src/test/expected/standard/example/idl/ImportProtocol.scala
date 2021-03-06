/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package example.idl

import other.ns.{ExternalDependency, Suit}

sealed trait ImportProtocol extends Product with Serializable

final case class DependentRecord(dependency: ExternalDependency, number: Int) extends ImportProtocol

final case class DependentRecord2(dependency: Suit.Value, name: String) extends ImportProtocol

final case class DependentRecord3(dependency: Embedded, value: Boolean) extends ImportProtocol