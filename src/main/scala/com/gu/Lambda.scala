package com.gu

import com.amazonaws.services.s3.model.S3Object
import okhttp3._
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.Json

import scala.annotation.tailrec
import scala.io.Source

object Lambda {

  val http = new OkHttpClient()
  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def handler() = {
    val newLabels = getAllDeskLabels()
    val oldLabels = getOldLabelsFromS3()
    val added = newLabels.diff(oldLabels)
    val deleted = oldLabels.diff(newLabels)
    logger.info(s"New labels = ${if (added.isEmpty) added.size else added.mkString}")
    logger.info(s"Deleted labels = ${if (deleted.isEmpty) deleted.size else deleted.mkString}")

    added.foreach(notifySlack(_))
    deleted.foreach(notifySlack(_, true))

    if (!added.isEmpty || !deleted.isEmpty) saveLabelsToS3(newLabels)
  }

  def getAllDeskLabels(): List[String] = {
    @tailrec
    def recursiveLabelsGet(namesList: List[String], path: String): List[String] = {
      val deskPage = DeskApiPage(path)(http, logger)
      deskPage.nextPage match {
        case None => namesList ::: deskPage.labels
        case Some(url) => recursiveLabelsGet(namesList ::: deskPage.labels, url)
      }
    }
    recursiveLabelsGet(List(), "/api/v2/labels?per_page=1000")
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
      slackPost(s"Label DELETED: $label")
    } else {
      logger.info(s"Notifying slack of label $label addition")
      slackPost(s"New label ADDED: $label")
    }
  }

}

object TestIt {
  def main(args: Array[String]): Unit = {
    Lambda.handler()
  }
}