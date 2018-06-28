package com.gu

import java.io.File

import com.gu.conf.{ConfigurationLoader, FileConfigurationLocation, SSMConfigurationLocation}

object Config {

  val identity = AppIdentity.whoAmI(defaultAppName = "desk-labels-slack-lambda")
  val config = ConfigurationLoader.load(identity) {
    case AwsIdentity(app, stack, stage, _) => SSMConfigurationLocation(app)
    case DevIdentity(app) if sys.props("testing") == "true" => FileConfigurationLocation(new File("unit-test.conf"))
  }

  val deskUrl = config.getString("desk.url")
  val deskEmail = config.getString("desk.email")
  val deskPass = config.getString("desk.pass")
  val s3Bucket = config.getString("s3.bucket")
  val s3Key = config.getString("s3.key")
  val hangoutsUrl = config.getString("hangouts.url")
  val appsHangoutsUrl = config.getString("hangouts.appsUrl")

  val appsLabelPrefixes: List[String] = List(
    "INA",
    "ANA",
    "IDE"
  )
}