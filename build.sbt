name := "sdkman-mongodb-persistence"

version := "0.2"

organization := "io.sdkman"

scalaVersion := "2.12.5"

crossScalaVersions := Seq("2.11.8", "2.12.5")

parallelExecution in Test := false

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1",
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

bintrayOrganization := Some("sdkman")

bintrayReleaseOnPublish in ThisBuild := true

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))