package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import io.sdkman.repos
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.{Completed, ScalaObservable}

import scala.concurrent.Future

trait VersionsRepo {

  self: MongoConnectivity =>

  import repos.mongoExecutionContext

  def saveVersion(v: Version): Future[Completed] = versionsCollection.insertOne(v).head()

  def findAllVersionsByCandidatePlatform(candidate: String, platform: String): Future[Seq[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("platform", platform)))
      .sort(ascending("version"))
      .map(doc => doc: Version)
      .toFuture()

  def findAllVersionsByCandidateVersion(candidate: String, version: String): Future[Seq[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version)))
      .map(doc => doc: Version)
      .toFuture()

  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .map(doc => doc: Version)
      .toFuture()
      .map(_.headOption)
}

case class Version(candidate: String, version: String, platform: String, url: String)