package com.gu

import com.gu.conf.{ConfigurationLoader, ConfigurationLocation, SSMConfigurationLocation}

case class SSMConfigurationLocationForLambda(override val path: String, override val region: String = "eu-west-1") extends SSMConfigurationLocation(path, region)

object SSMConfigurationLocationForLambda {
  def default(identity: AwsIdentity): ConfigurationLocation = {
    SSMConfigurationLocation(s"/${identity.stage}/${identity.stack}/${identity.app}")
  }
}

case class Env(app: String, stack: String, stage: String) {
  override def toString: String = s"App: $app, Stack: $stack, Stage: $stage"
}

object Env {
  def apply(): Env = Env(
    Option(System.getenv("App")).getOrElse("DEV"),
    Option(System.getenv("Stack")).getOrElse("DEV"),
    Option(System.getenv("Stage")).getOrElse("DEV")
  )
}

object Config {

  val env = Env()

  val identity: AppIdentity = if (env.stage == "DEV") {
    DevIdentity("desk-labels-slack-lambda")
  } else {
    AwsIdentity(env.app, env.stack, env.stage, "eu-west-1")
  }
  val config = ConfigurationLoader.load(identity) {
    case identity: AwsIdentity => SSMConfigurationLocationForLambda.default(identity)
  }

  val deskUrl = config.getString("desk.url")
  val deskEmail = config.getString("desk.email")
  val deskPass = config.getString("desk.pass")
  val s3Bucket = config.getString("s3.bucket")
  val s3Key = config.getString("s3.key")
  val slackUrl = config.getString("slack.url")

}