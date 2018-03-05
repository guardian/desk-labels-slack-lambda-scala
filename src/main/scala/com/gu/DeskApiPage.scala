package com.gu

import okhttp3.{Credentials, OkHttpClient, Request}
import org.slf4j.Logger
import play.api.libs.json.{JsValue, Json}

case class DeskApiPage(path: String)(implicit http: OkHttpClient, logger: Logger) {
  val js: JsValue = {
    logger.info(s"Retrieving labels from $path")
    val response = http.newCall(new Request.Builder()
      .url(s"https://${Config.deskUrl}${path}")
      .addHeader("Accept", "application/json")
      .addHeader("Authorization", Credentials.basic(Config.deskEmail, Config.deskPass))
      .build).execute()
    Json.parse(response.body().string())
  }

  val labels: List[String] = {
    logger.info("Parsing page of desk labels")
    val names = js \ "_embedded" \ "entries" \\ "name"
    names.map(_.toString()).toList
  }

  val nextPage: Option[String] = (js \ "_links" \ "next" \ "href").asOpt[String]
}
