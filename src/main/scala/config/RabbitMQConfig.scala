package config

import com.rabbitmq.client.{Connection, ConnectionFactory}
import com.typesafe.config.ConfigFactory

import scala.util.{Success, Failure,Try}

object RabbitMQConfig {
  private val config = ConfigFactory.load()
  private val rabbitmqConfig = config.getConfig("rabbitmq")

  private lazy val connectionFactory: ConnectionFactory = {
    val connectionFactory = new ConnectionFactory()
    connectionFactory.setHost(rabbitmqConfig.getString("host"))
    connectionFactory.setPort(rabbitmqConfig.getInt("port"))
    connectionFactory.setUsername(rabbitmqConfig.getString("username"))
    connectionFactory.setPassword(rabbitmqConfig.getString("password"))
    connectionFactory
  }


  def getConnection: Option[Connection] ={
     Try(connectionFactory.newConnection()) match{
       case Success(connection) =>Some(connection)
       case Failure(exception) =>
         println(s"Error create Connection: ${exception.getMessage}")
         None

     }
  }
}
