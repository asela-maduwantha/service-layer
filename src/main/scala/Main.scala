import consumer.Consumer
import repositories.EmployeeRepository
import services.EmployeeService
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
    val dbTry = Try(Database.forConfig("db"))

    dbTry match {
      case Success(db) =>
        sys.addShutdownHook {
          db.close()
        }

        val employeeRepository = new EmployeeRepository(db)
        val employeeService = new EmployeeService(employeeRepository)
        val consumer = new Consumer(employeeService)

        consumer.startConsuming()
        Thread.currentThread().join()

      case Failure(exception) =>
        println(s"Failed to establish database connection: ${exception.getMessage}")
        System.exit(1)
    }
  }
}
