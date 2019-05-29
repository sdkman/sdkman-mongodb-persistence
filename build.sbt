name := "sdkman-mongodb-persistence"

organization := "io.sdkman"

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.11.12", "2.12.8")

parallelExecution in Test := false

resolvers += Resolver.jcenterRepo

bintrayOrganization := Some("sdkman")

bintrayRepository := "maven"

bintrayReleaseOnPublish in ThisBuild := true

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0",
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test)

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
