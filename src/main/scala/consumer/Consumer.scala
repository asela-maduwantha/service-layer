package consumer

import com.rabbitmq.client.{AMQP, Channel, Connection, DefaultConsumer, DeliverCallback, Envelope}
import com.typesafe.config.ConfigFactory
import config.RabbitMQConfig

import scala.util.{Failure, Success, Try, Using}
import io.circe.parser.decode

object Consumer  {
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
