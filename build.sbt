name := "desk-labels-slack-lambda"

organization := "com.gu"

description:= "Notify Slack when a new label is added to Desk.com"

version := "1.0"

scalaVersion := "2.12.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-target:jvm-1.8",
  "-Ywarn-dead-code"
)

val circeVersion = "0.7.0"

testOptions += Tests.Setup(_ => sys.props("testing") = "true")

resolvers += "Guardian Platform Bintray" at "https://dl.bintray.com/guardian/platforms"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.25",
  "com.typesafe" % "config" % "1.3.2",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.squareup.okhttp3" % "okhttp" % "3.10.0",
  "com.gu" %% "simple-configuration-ssm" % "1.5.0",
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.288"
)

enablePlugins(JavaAppPackaging, RiffRaffArtifact)

topLevelDirectory in Universal := None
packageName in Universal := normalizedName.value

riffRaffPackageType := (packageBin in Universal).value
riffRaffUploadArtifactBucket := Option("riffraff-artifact")
riffRaffUploadManifestBucket := Option("riffraff-builds")
riffRaffArtifactResources += (file("cfn.yaml"), s"${name.value}-cfn/cfn.yaml")
