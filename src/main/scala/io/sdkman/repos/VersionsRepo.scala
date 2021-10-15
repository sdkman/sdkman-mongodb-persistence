package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import org.mongodb.scala.{Completed, ScalaObservable, SingleObservable}

import scala.collection.immutable.TreeMap
import scala.concurrent.Future
import scala.math.Ordered.orderingToOrdered

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

sealed trait ChecksumAlgorithm extends Ordering[ChecksumAlgorithm] {
  def id: String
  def rank : Integer

  def compare(x: ChecksumAlgorithm, y: ChecksumAlgorithm): Int = x.rank compare y.rank
}

case object MD5 extends ChecksumAlgorithm {
  override val id = "MD5"
  override val rank = 1
}

case object SHA1 extends ChecksumAlgorithm {
  override val id = "SHA-1"
  override val rank = 2
}

case object SHA224 extends ChecksumAlgorithm {
  override val id = "SHA-224"
  override val rank = 3
}

case object SHA256 extends ChecksumAlgorithm {
  override val id = "SHA-256"
  override val rank = 4
}

case object SHA384 extends ChecksumAlgorithm {
  override val id = "SHA-384"
  override val rank = 5
}

case object SHA512 extends ChecksumAlgorithm {
  override val id = "SHA-512"
  override val rank = 6
}

case class Version(
    candidate: String,
    version: String,
    platform: String,
    url: String,
    vendor: Option[String] = None,
    visible: Option[Boolean] = Some(true),
    checksums: Option[TreeMap[String, String]] = None
)
