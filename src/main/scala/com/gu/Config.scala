package com.gu

import com.gu.conf.{ConfigurationLoader, SSMConfigurationLocation}

object Config {

  val identity = AppIdentity.whoAmI(defaultAppName = "desk-labels-slack-lambda")
  val config = ConfigurationLoader.load(identity) {
    case identity: AwsIdentity => SSMConfigurationLocation.default(identity)
  }

  val deskUrl = config.getString("desk.url")
  val deskEmail = config.getString("desk.email")
  val deskPass = config.getString("desk.pass")
  val s3Bucket = config.getString("s3.bucket")
  val s3Key = config.getString("s3.key")
  val hangoutsUrl = config.getString("hangouts.url")

}