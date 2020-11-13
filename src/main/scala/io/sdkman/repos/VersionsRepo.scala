package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.{Completed, ScalaObservable}

import scala.concurrent.Future

trait VersionsRepo {

  self: MongoConnectivity =>

  def saveVersion(v: Version): Future[Completed] = versionsCollection.insertOne(v).head()

  def findAllVersionsByCandidatePlatform(candidate: String, platform: String): Future[Seq[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate),
        or(equal("platform", platform), equal("platform", "UNIVERSAL")),
        or(equal("visible", true), not(exists("visible")))))
      .sort(ascending("version"))
      .toFuture()

  def findAllVersionsByCandidateVersion(candidate: String, version: String): Future[Seq[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version)))
      .toFuture()

  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version), equal("platform", platform)))
      .first
      .toFuture()
      .map(_.headOption)
}

case class Version(candidate: String,
                   version: String,
                   platform: String,
                   url: String,
                   vendor: Option[String] = None,
                   visible: Option[Boolean] = Some(true))