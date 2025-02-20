import consumer.Consumer
import repositories.EmployeeRepository
import services.EmployeeService
import slick.jdbc.PostgresProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.concurrent.Await
import scala.concurrent.duration._

object Main {
  def main(args: Array[String]): Unit = {
    val dbTry = Try(Database.forConfig("db"))

    dbTry.flatMap { db =>
      Try {
        val employeeRepository = new EmployeeRepository(db)
        Await.result(employeeRepository.createTableIfNotExists(), 30.seconds)

        val employeeService = new EmployeeService(employeeRepository)
        val consumer        = new Consumer(employeeService)

        consumer.startConsuming()

        sys.addShutdownHook {
          println("Shutting down application...")
          Try(db.close()).failed.foreach(e => println(s"Database close error: ${e.getMessage}"))
        }

        // Keep the main thread alive
        Thread.currentThread().join()
      }.recoverWith {
        case ex =>
          println(s"Application error: ${ex.getMessage}")
          Try(db.close())
          Failure(ex)
      }
    } match {
      case Success(_) =>
        println("Application started successfully.")
      case Failure(ex) =>
        println(s"Failed to initialize application: ${ex.getMessage}")
        System.exit(1)
    }
  }
}
