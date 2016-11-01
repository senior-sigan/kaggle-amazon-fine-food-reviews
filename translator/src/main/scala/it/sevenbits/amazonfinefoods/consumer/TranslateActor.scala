package it.sevenbits.amazonfinefoods.consumer

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest}
import akka.stream.ActorMaterializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.concurrent.duration.DurationLong
import scala.util.{Failure, Success}

class TranslateActor extends Actor {
  implicit val system: ActorSystem = context.system

  import system.dispatcher

  implicit val materializer = ActorMaterializer()
  val objectMapper: ObjectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  override def receive: Receive = {
    case msg: String => translate(msg)
  }

  def translate(data: String): Unit = {
    val json = objectMapper.writeValueAsString(TranslateMessage(data))
    val fResponse = Http().singleRequest(HttpRequest(POST, uri = "http://localhost:9000/translate", entity = HttpEntity(`application/json`, json)))
    fResponse onComplete {
      case Success(response) =>
        response.entity.toStrict(300.millis).map(_.data).map(_.utf8String).onComplete { body =>
          println("===================================>")
          println(data)
          println(body.get)
          println("<===================================")
        }
      case Failure(t) => println(s"Fail: ${t.getMessage}")
    }
    // TODO: put this message in DB or queue
    // TODO: join data in the initial sentences
  }
}
