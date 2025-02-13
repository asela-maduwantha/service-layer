import consumer.Consumer

object Main {
  def main(args: Array[String]): Unit = {
    Consumer.startConsuming()

    // Keep the main thread alive indefinitely
    Thread.currentThread().join()
  }
}
