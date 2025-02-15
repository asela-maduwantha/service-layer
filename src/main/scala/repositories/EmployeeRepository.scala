package repositories
import slick.jdbc.PostgresProfile.api._
import models.Employee
import scala.concurrent.{Future, ExecutionContext}

class EmployeeRepository(db: Database)(implicit ec: ExecutionContext) {


  def create(employee: Employee): Future[Int] = {
    val insertAction = (Employee.table returning Employee.table.map(_.id)) += employee
    db.run(insertAction).recoverWith {
      case ex: Exception =>
        Future.failed(ex)
    }
  }
}
