package repositories

import slick.jdbc.PostgresProfile.api._
import models.Employee

import scala.concurrent.{ExecutionContext, Future}

class EmployeeRepository(db: Database)(implicit ec: ExecutionContext) {
  def createTableIfNotExists(): Future[Unit] = {
    db.run(Employee.table.schema.createIfNotExists)
  }

  //create a new employee
  def create(employee: Employee): Future[Int] = {
    val insertAction = (Employee.table returning Employee.table.map(_.id)) += employee
    db.run(insertAction).recoverWith {
      case ex: Exception =>
        println(s"Database error: ${ex.getMessage}")
        Future.failed(ex)
    }
  }

  //get an employee by id
  def getById(id: Int): Future[Option[Employee]] = {
    db.run(Employee.table.filter(_.id === id).result.headOption)
  }

  //update an employee
  def update(employee: Employee): Future[Int] = {
    employee.id match{
      case Some(id) =>
        db.run(Employee.table.filter(_.id === id).update(employee))

      case None =>
        Future.failed(new SlickException("Employee not found"))
    }
  }

  //delete an employee by id
  def delete(id: Int): Future[Int] = {
    db.run(Employee.table.filter(_.id === id).delete)
  }


}