package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import io.sdkman.model.Application

import scala.concurrent.Future

trait ApplicationRepo {

  self: MongoConnectivity =>

  def findApplication(): Future[Option[Application]] = appCollection.find().headOption()

}
