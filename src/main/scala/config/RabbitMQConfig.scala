package config

import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory

object RabbitMQConfig {
  private val config = ConfigFactory.load()
  private val rabbitmqConfig = config.getConfig("rabbitmq")

  private lazy val factory: ConnectionFactory = {
    val f = new ConnectionFactory()
    f.setHost(rabbitmqConfig.getString("host"))
    f.setPort(rabbitmqConfig.getInt("port"))
    f.setUsername(rabbitmqConfig.getString("username"))
    f.setPassword(rabbitmqConfig.getString("password"))
    f
  }

  def getConnectionFactory: ConnectionFactory = factory
}
