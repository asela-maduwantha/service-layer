package repositories

import slick.jdbc.PostgresProfile.api._
import models.Employee
import scala.concurrent.{Future, ExecutionContext}

class EmployeeRepository(db: Database)(implicit ec: ExecutionContext) {
  def createTableIfNotExists(): Future[Unit] = {
    db.run(Employee.table.schema.createIfNotExists)
  }

  def create(employee: Employee): Future[Int] = {
    val insertAction = (Employee.table returning Employee.table.map(_.id)) += employee
    db.run(insertAction).recoverWith {
      case ex: Exception =>
        println(s"Database error: ${ex.getMessage}")
        Future.failed(ex)
    }
  }
}