package it.sevenbits.amazonfinefoods.api

import java.net.{URL, URLEncoder}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.twitter.finatra.http.Controller

class TranslateController extends Controller {
  /**
    * Yes, I know. This should not be here, but anyway.
    */
  val key = "trnsl.1.1.20161101T071032Z.da34b4be1082deb4.b7baebf9a3c5a7ff6063c554821134a596b60d3f"
  val objectMapper: ObjectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  post("/translate") { msg: TranslateMessage =>
    val text = URLEncoder.encode(msg.data, "UTF-8")
    println(msg.data)
    val url = s"https://translate.yandex.net/api/v1.5/tr.json/translate?key=trnsl.1.1.20161101T071032Z.da34b4be1082deb4.b7baebf9a3c5a7ff6063c554821134a596b60d3f&lang=en-ru&text=$text"
    val model = objectMapper.readValue(new URL(url), classOf[YandexTranslateMessage])
    println(model)
    model.text.head
  }
}
