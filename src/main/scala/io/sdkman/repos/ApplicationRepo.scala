package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import io.sdkman.repos

import scala.concurrent.Future

class ApplicationRepo {

  self: MongoConnectivity =>

  import repos.mongoExecutionContext

  def findApplication(): Future[Option[Application]] =
    appCollection
      .find()
      .first()
      .map(doc => doc: Application)
      .toFuture()
      .map(_.headOption)

}

case class Application(alive: String, stableCliVersion: String, betaCliVersion: String)
