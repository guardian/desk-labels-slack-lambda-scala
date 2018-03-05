package com.gu

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{AWSCredentialsProviderChain, DefaultAWSCredentialsProviderChain}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder

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
