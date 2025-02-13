package models

import java.time.LocalDate
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape


case class Employee(id: Option[Int], name: String, dob: LocalDate, email: String)

class Employees(tag: Tag) extends Table[Employee](tag, "employees") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def dob = column[LocalDate]("dob")
  def email = column[String]("email")

  override def * : ProvenShape[Employee] =
    (id.?, name, dob, email) <> ((Employee.apply _).tupled, Employee.unapply)
}

object Employee {
  val table = TableQuery[Employees]
}
