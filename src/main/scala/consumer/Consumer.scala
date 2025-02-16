package consumer

import com.rabbitmq.client.{Channel, Connection, DeliverCallback}
import com.typesafe.config.ConfigFactory
import config.RabbitMQConfig
import io.circe.jawn.decode
import models.Employee

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import services.EmployeeService
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps

class Consumer(employeeService: EmployeeService) {
  private val config = ConfigFactory.load()
  private val rabbitmqConfig = config.getConfig("rabbitmq")
  private val QUEUE_NAME = rabbitmqConfig.getString("queueName")

  private var channel: Option[Channel] = None
  private var connection: Option[Connection] = None

  def startConsuming(): Unit = {
    Try(RabbitMQConfig.getConnectionFactory.newConnection()) match {
      case Success(conn: Connection) =>
        connection = Some(conn)
        Try(conn.createChannel()) match {
          case Success(ch: Channel) =>
            channel = Some(ch)

              Try(ch.queueDeclare(QUEUE_NAME, false, false, false, null)) match{
                case Success(_)=>
                  val deliverCallback: DeliverCallback = (_, delivery) => {
                    val bodyStr = new String(delivery.getBody, "UTF-8")
                    val props   = delivery.getProperties
                    val replyTo = props.getReplyTo
                    val correlationId = props.getCorrelationId

                    println(s"Received RPC request: $bodyStr")

                    def sendResponse(response: String): Unit = {
                      val replyProps = new com.rabbitmq.client.AMQP.BasicProperties.Builder()
                        .correlationId(correlationId)
                        .build()
                      if (replyTo != null && replyTo.nonEmpty)
                        ch.basicPublish("", replyTo, replyProps, response.getBytes("UTF-8"))
                      ch.basicAck(delivery.getEnvelope.getDeliveryTag, false)
                    }

                    val parts = bodyStr.split(":", 2)
                    if (parts.length != 2) {
                      println("Bad request")
                      sendResponse("""{"status": "error", "message": "Bad request"}""")
                    } else {
                      val operation = parts(0)
                      val payload   = parts(1)
                      operation match {
                        case "create" =>
                          decode[Employee](payload) match {
                            case Right(employee) =>
                              employeeService.addEmployee(employee).onComplete {
                                case Success(id) =>
                                  sendResponse(s"""{"status": "success", "id": $id}""")
                                case Failure(ex) =>
                                  sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""")
                              }
                            case Left(error) =>
                              sendResponse(s"""{"status": "error", "message": "Invalid employee data: $error"}""")
                          }
                        case "get" =>
                          Try(payload.toInt) match {
                            case Success(id) =>
                              employeeService.getEmployeeById(id).onComplete {
                                case Success(Some(employee)) =>
                                  sendResponse(s"""{"status": "success", "employee": ${employee.asJson.noSpaces}}""")
                                case Success(None) =>
                                  sendResponse(s"""{"status": "error", "message": "Employee not found"}""")
                                case Failure(ex) =>
                                  sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""")
                              }
                            case Failure(ex) =>
                              sendResponse(s"""{"status": "error", "message": "Invalid id: ${ex.getMessage}"}""")
                          }
                        case "update" =>
                          decode[Employee](payload) match {
                            case Right(employee) =>
                              employeeService.updateEmployee(employee).onComplete {
                                case Success(count) =>
                                  if (count > 0)
                                    sendResponse(s"""{"status": "success", "updated": $count}""")
                                  else
                                    sendResponse(s"""{"status": "error", "message": "Employee not updated"}""")
                                case Failure(ex) =>
                                  sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""")
                              }
                            case Left(error) =>
                              sendResponse(s"""{"status": "error", "message": "Invalid employee data: $error"}""")
                          }
                        case "delete" =>
                          Try(payload.toInt) match {
                            case Success(id) =>
                              employeeService.deleteEmployeeById(id).onComplete {
                                case Success(count) =>
                                  if (count > 0)
                                    sendResponse(s"""{"status": "success", "deleted": $count}""")
                                  else
                                    sendResponse(s"""{"status": "error", "message": "Employee not found"}""")
                                case Failure(ex) =>
                                  sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""")
                              }
                            case Failure(ex) =>
                              sendResponse(s"""{"status": "error", "message": "Invalid id: ${ex.getMessage}"}""")
                          }
                        case _ =>
                          sendResponse(s"""{"status": "error", "message": "Unknown operation"}""")
                      }
                    }
                  }

                  ch.basicConsume(QUEUE_NAME, false, deliverCallback, (_: String) => ())

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