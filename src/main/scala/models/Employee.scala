package models

import io.circe.Decoder
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import io.circe.generic.semiauto.deriveDecoder

case class Employee(id: Option[Int], name: String, email: String, address: String, salary: Double)

class Employees(tag: Tag) extends Table[Employee](tag, "employees") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def email = column[String]("email")
  def address = column[String]("address")
  def salary =column[Double]("salary")

  override def * : ProvenShape[Employee] =
    (id.?, name, email, address, salary) <> ((Employee.apply _).tupled, Employee.unapply)
}

object Employee {
  val table = TableQuery[Employees]
  implicit val employeeDecoder: Decoder[Employee] = deriveDecoder[Employee]
}
