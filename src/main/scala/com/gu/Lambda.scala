package com.gu

import com.amazonaws.auth.{AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain}
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.model.S3Object
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import okhttp3._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

case class SlackPayload(text: String)

object SlackPayload { implicit val slackPayloadWrites = Json.writes[SlackPayload] }

object Lambda {

  val http = new OkHttpClient()
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def handler() = {
    val newLabels = getLabelsFromResponse(getDeskResponse())
    val oldLabels = getOldLabelsFromS3()
    val added = newLabels.diff(oldLabels)
    val deleted = oldLabels.diff(newLabels)
    logger.info(s"New labels = ${if (added.size == 0) added.size else added.mkString}")
    logger.info(s"Deleted labels = ${if (deleted.size == 0) deleted.size else deleted.mkString}")

    added.foreach(notifySlack(_))
    deleted.foreach(notifySlack(_, true))

    if (!added.isEmpty || !deleted.isEmpty) saveLabelsToS3(newLabels)
  }

  def getDeskResponse(): Response = {
    //TODO: Make this recursive for additional pages
    logger.info("Retrieving labels from desk.com")
    val req = new Request.Builder()
      .url(new HttpUrl.Builder()
        .scheme("https")
          .host(Config.deskUrl)
          .addPathSegment("api")
          .addPathSegment("v2")
          .addPathSegment("labels")
          .addQueryParameter("per_page", "1000")
          .build()
      )
      .addHeader("Accept", "application/json")
      .addHeader("Authorization", Credentials.basic(Config.deskEmail, Config.deskPass))
      .build
    http.newCall(req).execute()
  }

  def getLabelsFromResponse(response: Response): List[String] = {
    logger.info("Parsing new labels")
    val json: JsValue = Json.parse(response.body().string())
    val names = json \ "_embedded" \ "entries" \\ "name"
    names.map(_.toString()).toList
  }

  def getOldLabelsFromS3(): List[String] = {
    logger.info("Retrieving old labels from S3")
    val obj: S3Object = AWS.s3Client.getObject(Config.s3Bucket, Config.s3Key)
    val content = Source.fromInputStream(obj.getObjectContent).mkString
    obj.close()
    content.split(",").map(_.trim).toList
  }

  def saveLabelsToS3(labels: List[String]) = {
    logger.info("Saving new labels to S3")
    AWS.s3Client.putObject(Config.s3Bucket, Config.s3Key, labels.mkString(","))
  }

  def notifySlack(label: String, isDelete: Boolean = false) = {
    def slackPost(message: String): Response = {
      val post = new Request.Builder()
        .url(Config.slackUrl)
        .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Json.toJson(SlackPayload(message)).toString()))
        .build()
      http.newCall(post).execute()
    }
    if (isDelete) {
      logger.info(s"Notifying slack of label $label deletion")
      slackPost(s"Label DELETED: $label - scala")
    } else {
      logger.info(s"Notifying slack of label $label addition")
      slackPost(s"New label ADDED: $label - scala")
    }
  }

}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.handler()
  }
}

object AWS {
  val region = Option(Regions.getCurrentRegion).map(r => Regions.fromName(r.getName)).getOrElse(Regions.EU_WEST_1)
  val s3Client = AmazonS3ClientBuilder
    .standard()
    .withRegion(region)
    .withCredentials(new AWSCredentialsProviderChain(
      new ProfileCredentialsProvider("mobile"),
      new DefaultAWSCredentialsProviderChain
      )
    ).build()
}