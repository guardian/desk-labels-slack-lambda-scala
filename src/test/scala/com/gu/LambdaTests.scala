package com.gu

import org.scalatest.{FunSuite, Matchers}

class LambdaTests extends FunSuite with Matchers {

  test("Apps labels sent to apps endpoint") {
    for (l <- Config.appsLabelPrefixes) Lambda.getHangoutsEndpoint(l) should be (Config.appsHangoutsUrl)
  }

  test("Non apps labels go to userhelp endpoint") {
    Lambda.getHangoutsEndpoint("NON apps url") should be (Config.hangoutsUrl)
  }

  test("Short labels go to userhelp endpoint") {
    Lambda.getHangoutsEndpoint("an") should be (Config.hangoutsUrl)
  }

  test("Lower case app labels go to userhelp endpoint") {
    /* This is to avoid valid words that also appear in apps prefixes, eg 'anaconda' being mistreated */
    Lambda.getHangoutsEndpoint(Config.appsLabelPrefixes.head.toLowerCase()) should be (Config.hangoutsUrl)
  }

}
