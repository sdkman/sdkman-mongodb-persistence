package io.sdkman

import java.util.concurrent.Executors

import org.mongodb.scala.bson._

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

package object repos {

  implicit val mongoExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  val MissingField = "MISSING_FIELD"

  implicit def versionToDocument(v: Version): Document =
    Document(
      "candidate" -> v.candidate,
      "version" -> v.version,
      "platform" -> v.platform,
      "url" -> v.url)

  implicit def documentToCandidate(doc: Document): Candidate =
    Candidate(
      field("candidate", doc),
      field("name", doc),
      field("description", doc),
      field("default", doc),
      field("websiteUrl", doc),
      field("distribution", doc))

  implicit def documentToVersion(doc: Document): Version =
    Version(
      field("candidate", doc),
      field("version", doc),
      field("platform", doc),
      field("url", doc))

  implicit def documentToApplication(doc: Document): Application =
    Application(
      field("alive", doc),
      field("stableCliVersion", doc),
      field("betaCliVersion", doc))

  private def field(n: String, d: Document): String =
    d.get[BsonString](n).map(_.asString.getValue).getOrElse(MissingField)
}

