name := "sdkman-mongodb-persistence"

version := "0.1"

scalaVersion := "2.12.5"

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.2.1",
  "com.typesafe" % "config" % "1.3.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)
