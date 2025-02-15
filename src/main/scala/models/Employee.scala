package models

import io.circe.Decoder
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax._

case class Employee(id: Option[Int], name: String, email: String)

class Employees(tag: Tag) extends Table[Employee](tag, "employees") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")

  override def * : ProvenShape[Employee] =
    (id.?, name, email) <> ((Employee.apply _).tupled, Employee.unapply)
}

object Employee {
  val table = TableQuery[Employees]

  // Explicit decoder for the 'id' field to handle Option[Int]
  implicit val employeeDecoder: Decoder[Employee] = deriveDecoder[Employee]
}
