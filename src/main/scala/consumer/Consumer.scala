package consumer

import com.rabbitmq.client.{Connection, DeliverCallback}
import com.typesafe.config.ConfigFactory
import config.RabbitMQConfig
import io.circe.jawn.decode
import models.Employee
import scala.concurrent.ExecutionContext.Implicits.global

import scala.util.{Failure, Success, Try}
import services.EmployeeService

class Consumer(employeeService: EmployeeService)  {
  private val config = ConfigFactory.load()
  private val rabbitmqConfig = config.getConfig("rabbitmq")

  private val QUEUE_NAME = rabbitmqConfig.getString("queueName")

  def startConsuming(): Unit = {
    Try(RabbitMQConfig.getConnectionFactory.newConnection()) match {
      case Success(connection: Connection) =>
        val channel = connection.createChannel()
          channel.queueDeclare(QUEUE_NAME, false, false, false, null)

          val deliverCallback: DeliverCallback = (_, delivery) => {
            Try(new String(delivery.getBody, "UTF-8")) match {
              case Success(request) =>
                println(request)
                decode [Employee](request) match{
                  case Right(employee) =>
                    employeeService.addEmployee(employee).onComplete {
                      case Success(id) => println(s"Employee saved with ID: $id")
                      case Failure(exception) => println(s"Failed to save employee: ${exception.getMessage}")
                    }
                  case Left(error) => println(s"Bad Request: $error")
                }
              case Failure(exception) =>
                println(s"Failed to convert delivery body: ${exception.getMessage}")
            }
          }
          channel.basicConsume(QUEUE_NAME, true, deliverCallback, (consumerTag: String) => {})

      case Failure(exception) =>
        println(s"Failed to connect to RabbitMQ: ${exception.getMessage}")
    }
  }
}
