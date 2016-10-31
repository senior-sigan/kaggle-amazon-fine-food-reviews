package it.sevenbits.amazonfinefoods.consumer

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TranslateActor extends Actor {
  implicit val system: ActorSystem = context.system
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def receive: Receive = {
    case msg: String => translate(msg)
  }

  def translate(data: String): Unit = {
    println(data)
    val fResponse = Http().singleRequest(HttpRequest(POST, uri = "http://localhost:9000/translate", entity = data))
    val fResult = {
      for {
        resp <- fResponse
      } yield {
        resp.entity.toString
      }
    }

    val result = Await.result(fResult, Duration.Inf)
    println(s"Success: $result")
    // TODO: put this message in DB or queue
    // TODO: join data in the initial sentences
  }
}
