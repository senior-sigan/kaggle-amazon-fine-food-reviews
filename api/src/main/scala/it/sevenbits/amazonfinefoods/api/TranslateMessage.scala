package it.sevenbits.amazonfinefoods.api

case class TranslateMessage(data: String)

case class YandexTranslateMessage(lang: String, text: List[String], code: Int)