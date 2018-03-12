package com.gu

import play.api.libs.json.Json

case class HangoutsPayload(text: String)

object HangoutsPayload { implicit val hangoutsPayloadWrites = Json.writes[HangoutsPayload] }