package it.sevenbits.amazonfinefoods.consumer

import akka.actor.{Actor, ActorSystem, Props}
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory}

import scala.concurrent.duration.DurationDouble

object Consumer extends App {
  val queueName = "translate_queue"
  implicit val system = ActorSystem()
  val factory = new ConnectionFactory()
  factory.setHost("172.17.0.2")
  val connection: Connection = factory.newConnection()
  val channel: Channel = connection.createChannel()

  startAkka(connection, channel)

  channel.close()
  connection.close()

  def fetch(channel: Channel): Option[String] = {
    val response = channel.basicGet(queueName, false)

    if (response != null) {
      channel.basicAck(response.getEnvelope.getDeliveryTag, false)
      Some(new String(response.getBody))
    } else {
      None
    }
  }

  def startAkka(connection: Connection, channel: Channel): Unit = {
    import system.dispatcher
    val translatorActor = system.actorOf(Props(classOf[TranslateActor]))
    system.scheduler.schedule(0.seconds, 1.seconds, translatorActor, fetch(channel).get)
  }
}