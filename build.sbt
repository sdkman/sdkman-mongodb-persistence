name := "sdkman-mongodb-persistence"

organization := "io.sdkman"

scalaVersion := "2.12.12"

crossScalaVersions := Seq("2.11.12", "2.12.12")

parallelExecution in Test := false

resolvers ++= Seq(
  Resolver.mavenCentral,
  "jitpack" at "https://jitpack.io"
)

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0",
  "com.typesafe" % "config" % "1.3.1",
  "com.github.sdkman" % "sdkman-persistent-model" % "1.1.2",
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "org.testcontainers" % "mongodb" % "1.16.0" % Test
)

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))
