package consumer

import com.rabbitmq.client.{DeliverCallback, Delivery}
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

  private val connection = RabbitMQConfig.getConnection.get

  private val channel = connection.createChannel()


  private def sendResponse(response: String, correlationId: String, replyTo: String, delivery: Delivery): Unit = {
    val replyProps = new com.rabbitmq.client.AMQP.BasicProperties.Builder()
      .correlationId(correlationId)
      .build()
    if (replyTo != null && replyTo.nonEmpty)
      channel.basicPublish("", replyTo, replyProps, response.getBytes("UTF-8"))
    channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
  }


  def startConsuming(): Unit = {
    Try(channel.queueDeclare(QUEUE_NAME, false, false, false, null)) match {
      case Success(_) =>
        val deliverCallback: DeliverCallback = (_, delivery) => {
          val bodyStr = new String(delivery.getBody, "UTF-8")
          val props = delivery.getProperties
          val replyTo = props.getReplyTo
          val correlationId = props.getCorrelationId

          val parts = bodyStr.split(":", 2)
          if (parts.length != 2) {
            println("Bad request")
            sendResponse("""{"status": "error", "message": "Bad request"}""", correlationId, replyTo, delivery)
          } else {
            val operation = parts(0)
            val payload = parts(1)
            operation match {
              case "create" =>
                decode[Employee](payload) match {
                  case Right(employee) =>
                    employeeService.addEmployee(employee).onComplete {
                      case Success(id) =>
                        sendResponse(s"""{"status": "success", "id": $id}""", correlationId, replyTo, delivery)
                      case Failure(ex) =>
                        sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""", correlationId, replyTo, delivery)
                    }
                  case Left(error) =>
                    sendResponse(s"""{"status": "error", "message": "Invalid employee data: $error"}""", correlationId, replyTo, delivery)
                }
              case "get" =>
                Try(payload.toInt) match {
                  case Success(id) =>
                    employeeService.getEmployeeById(id).onComplete {
                      case Success(Some(employee)) =>
                        sendResponse(s"""{"status": "success", "employee": ${employee.asJson.noSpaces}}""", correlationId, replyTo, delivery)
                      case Success(None) =>
                        sendResponse(s"""{"status": "error", "message": "Employee not found"}""", correlationId, replyTo, delivery)
                      case Failure(ex) =>
                        sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""", correlationId, replyTo, delivery)
                    }
                  case Failure(ex) =>
                    sendResponse(s"""{"status": "error", "message": "Invalid id: ${ex.getMessage}"}""", correlationId, replyTo, delivery)
                }
              case "update" =>
                decode[Employee](payload) match {
                  case Right(employee) =>
                    employeeService.updateEmployee(employee).onComplete {
                      case Success(count) =>
                        if (count > 0)
                          sendResponse(s"""{"status": "success", "updated": $count}""", correlationId, replyTo, delivery)
                        else
                          sendResponse(s"""{"status": "error", "message": "Employee not updated"}""", correlationId, replyTo, delivery)
                      case Failure(ex) =>
                        sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""", correlationId, replyTo, delivery)
                    }
                  case Left(error) =>
                    sendResponse(s"""{"status": "error", "message": "Invalid employee data: $error"}""", correlationId, replyTo, delivery)
                }
              case "delete" =>
                Try(payload.toInt) match {
                  case Success(id) =>
                    employeeService.deleteEmployeeById(id).onComplete {
                      case Success(count) =>
                        if (count > 0)
                          sendResponse(s"""{"status": "success", "deleted": $count}""", correlationId, replyTo, delivery)
                        else
                          sendResponse(s"""{"status": "error", "message": "Employee not found"}""", correlationId, replyTo, delivery)
                      case Failure(ex) =>
                        sendResponse(s"""{"status": "error", "message": "${ex.getMessage}"}""", correlationId, replyTo, delivery)
                    }
                  case Failure(ex) =>
                    sendResponse(s"""{"status": "error", "message": "Invalid id: ${ex.getMessage}"}""", correlationId, replyTo, delivery)
                }
              case _ =>
                sendResponse(s"""{"status": "error", "message": "Unknown operation"}""", correlationId, replyTo, delivery)
            }
          }
        }

        channel.basicConsume(QUEUE_NAME, false, deliverCallback, (_: String) => ())

    }

  }

}