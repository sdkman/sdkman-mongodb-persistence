package io.sdkman.repos

import io.sdkman.db.MongoConnectivity
import org.mongodb.scala._
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.model.Sorts.ascending
import org.mongodb.scala.model.Updates.set
import org.mongodb.scala.result.UpdateResult

import scala.concurrent.Future

trait CandidatesRepo {

  self: MongoConnectivity =>

  def findCandidate(candidate: String): Future[Option[Candidate]] =
    candidatesCollection
      .find(equal("candidate", candidate))
      .first
      .headOption()

  def findAllCandidates(): Future[Seq[Candidate]] =
    candidatesCollection
      .find()
      .sort(ascending("candidate"))
      .toFuture()

  def updateDefaultVersion(candidate: String, version: String): Future[UpdateResult] =
    candidatesCollection
      .updateOne(equal("candidate", candidate), set("default", version))
      .head()

  def insertCandidate(candidate: Candidate): Future[Completed] =
    candidatesCollection
      .insertOne(candidate)
      .toFuture()
}

case class Candidate(
    candidate: String,
    name: String,
    description: String,
    default: Option[String],
    websiteUrl: String,
    distribution: String
)
