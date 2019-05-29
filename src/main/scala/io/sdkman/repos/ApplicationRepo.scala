package io.sdkman.repos

import io.sdkman.db.MongoConnectivity

import scala.concurrent.Future

trait ApplicationRepo {

  self: MongoConnectivity =>

  def findApplication(): Future[Option[Application]] = appCollection.find().headOption()

}

case class Application(alive: String, stableCliVersion: String, betaCliVersion: String)
