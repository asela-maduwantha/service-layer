package consumer

import com.rabbitmq.client.{Channel, Connection, DeliverCallback}
import com.typesafe.config.ConfigFactory
import config.RabbitMQConfig
import io.circe.jawn.decode
import models.Employee
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import services.EmployeeService

class Consumer(employeeService: EmployeeService) {
  private val config = ConfigFactory.load()
  private val rabbitmqConfig = config.getConfig("rabbitmq")
  private val CONSUME_QUEUE_NAME = rabbitmqConfig.getString("consumeQueueName")

  private var channel: Option[Channel] = None
  private var connection: Option[Connection] = None

  def startConsuming(): Unit = {
    Try(RabbitMQConfig.getConnectionFactory.newConnection()) match {
      case Success(conn: Connection) =>
        connection = Some(conn)
        Try(conn.createChannel()) match {
          case Success(ch: Channel) =>
            channel = Some(ch)
            try {
              ch.queueDeclare(CONSUME_QUEUE_NAME, false, false, false, null)

              val deliverCallback: DeliverCallback = (_, delivery) => {
                Try(new String(delivery.getBody, "UTF-8")) match {
                  case Success(request) =>
                    println(s"Received data: $request")
                    decode[Employee](request) match {
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

              ch.basicConsume(QUEUE_NAME, true, deliverCallback, (_: String) => ())
            } catch {
              case ex: Exception =>
                println(s"Channel error: ${ex.getMessage}")
                cleanup()
            }
          case Failure(ex) =>
            println(s"Failed to create channel: ${ex.getMessage}")
            cleanup()
        }
      case Failure(ex) =>
        println(s"Failed to connect to RabbitMQ: ${ex.getMessage}")
    }
  }

  private def cleanup(): Unit = {
    channel.foreach(ch => Try(ch.close()))
    connection.foreach(conn => Try(conn.close()))
  }

  def shutdown(): Unit = {
    cleanup()
  }
}