package it.sevenbits.amazonfinefoods.api

import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.routing.HttpRouter

object Server extends HttpServer {
  override val defaultFinatraHttpPort: String = ":9000"

  override protected def configureHttp(router: HttpRouter): Unit = {
    router.add(new TranslateController())
  }
}
