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

  implicit def candidateToDocument(c: Candidate): Document =
    c.default.fold {
      Document(
        "candidate" -> c.candidate,
        "name" -> c.name,
        "description" -> c.description,
        "websiteUrl" -> c.websiteUrl,
        "distribution" -> c.distribution)
    } { default =>
      Document(
        "candidate" -> c.candidate,
        "name" -> c.name,
        "description" -> c.description,
        "default" -> default,
        "websiteUrl" -> c.websiteUrl,
        "distribution" -> c.distribution)
    }

  implicit def documentToCandidate(doc: Document): Candidate =
    Candidate(
      field("candidate", doc),
      field("name", doc),
      field("description", doc),
      optionalField("default", doc),
      field("websiteUrl", doc),
      field("distribution", doc))

  implicit def documentToVersion(doc: Document): Version =
    optionalField("vendor", doc).fold(
      Version(
        field("candidate", doc),
        field("version", doc),
        field("platform", doc),
        field("url", doc),
        None)) { vendor =>
      Version(
        field("candidate", doc),
        field("version", doc),
        field("platform", doc),
        field("url", doc),
        Some(vendor))
    }

  implicit def documentToApplication(doc: Document): Application =
    Application(
      field("alive", doc),
      field("stableCliVersion", doc),
      field("betaCliVersion", doc))

  private def field(n: String, d: Document): String =
    d.get[BsonString](n).map(_.asString.getValue).getOrElse(MissingField)

  private def optionalField(n: String, d: Document): Option[String] =
    d.get[BsonString](n).map(_.asString.getValue)

}

