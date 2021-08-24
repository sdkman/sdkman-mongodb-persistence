package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, ScalaObservable, SingleObservable}

import scala.concurrent.Future

trait VersionsRepo {

  self: MongoConnectivity =>

  def saveVersion(v: Version): Future[Completed] = versionsCollection.insertOne(v).head()

  def updateVersion(v: Version, updated: Version): Future[UpdateResult] =
    versionsCollection
      .replaceOne(
        and(
          equal("candidate", v.candidate),
          equal("version", v.version),
          equal("platform", v.platform)
        ),
        updated
      )
      .head()

  def deleteVersion(candidate: String, version: String, platform: String): Future[DeleteResult] =
    versionsCollection
      .deleteOne(
        and(
          equal("candidate", candidate),
          equal("version", version),
          equal("platform", platform)
        )
      )
      .head()

  // for backwards-compatibility
  def findAllVersionsByCandidatePlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]] =
    findAllVisibleVersionsByCandidatePlatform(candidate, platform)

  def findAllVisibleVersionsByCandidatePlatform(
      candidate: String,
      platform: String
  ): Future[Seq[Version]] =
    versionsCollection
      .find(
        and(
          equal("candidate", candidate),
          or(equal("platform", platform), equal("platform", "UNIVERSAL")),
          or(equal("visible", true), not(exists("visible")))
        )
      )
      .sort(ascending("version"))
      .toFuture()

  def findAllVersionsByCandidateVersion(candidate: String, version: String): Future[Seq[Version]] =
    versionsCollection
      .find(and(equal("candidate", candidate), equal("version", version)))
      .toFuture()

  def findVersion(candidate: String, version: String, platform: String): Future[Option[Version]] =
    versionsCollection
      .find(
        and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
      )
      .first
      .toFuture()
      .map(_.headOption)

  def findJavaVersionSeries(
      platform: String,
      majorVersion: Int,
      vendorSuffix: Any
  ): Future[Seq[Version]] =
    versionsCollection
      .find(
        and(
          equal("candidate", "java"),
          regex("version", s"^$majorVersion"),
          regex("version", s"\\.$vendorSuffix|[0-9]-$vendorSuffix$$"),
          equal("platform", platform)
        )
      )
      .toFuture()
}

case class Version(
    candidate: String,
    version: String,
    platform: String,
    url: String,
    vendor: Option[String] = None,
    visible: Option[Boolean] = Some(true),
    algorithm: Option[String] = None,
    checksum: Option[String] = None
)
