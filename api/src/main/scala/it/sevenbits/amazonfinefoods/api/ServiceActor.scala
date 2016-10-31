package it.sevenbits.amazonfinefoods.api

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

class ServiceActor extends Actor with Service {
  override def receive: Receive = runRoute(route)

  override def actorRefFactory = context
}

trait Service extends HttpService {
  val route: Route = path("translate") {
    post { it =>
      println(it.request.entity.asString)
      it.complete {
        //TODO: pong with text
        "hello world"
      }
    }
  }
}