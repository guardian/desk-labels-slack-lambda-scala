package com.gu

import play.api.libs.json.Json

case class SlackPayload(text: String)

object SlackPayload { implicit val slackPayloadWrites = Json.writes[SlackPayload] }