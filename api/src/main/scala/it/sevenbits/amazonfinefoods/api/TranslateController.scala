package it.sevenbits.amazonfinefoods.api

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

class TranslateController extends Controller {
  post("/translate") { request: Request =>
    println(request.encodeString())
    "hello world"
  }
}
