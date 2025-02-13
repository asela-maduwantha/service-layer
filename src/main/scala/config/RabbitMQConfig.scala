package config

import com.rabbitmq.client.ConnectionFactory
import com.typesafe.config.ConfigFactory




object RabbitMQConfig {
  private val config = ConfigFactory.load()
  private val rabbitmqConfig = config.getConfig("rabbitmq")

  private lazy val factory: ConnectionFactory = new ConnectionFactory()
  factory.setHost(rabbitmqConfig.getString("host"))
  factory.setPort(rabbitmqConfig.getInt("port"))
  factory.setUsername(rabbitmqConfig.getString("username"))
  factory.setPassword(rabbitmqConfig.getString("password"))

  def getConnectionFactory: ConnectionFactory = factory
}
