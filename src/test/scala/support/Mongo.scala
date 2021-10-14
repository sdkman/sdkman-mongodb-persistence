package support

import java.util.concurrent.TimeUnit
import io.sdkman.repos.{Application, Candidate, Version}
import org.mongodb.scala._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters.{and, equal}
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Mongo {

  import Helpers._
  import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
  import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
  import org.mongodb.scala.bson.codecs.Macros._

  val codecRegistry = fromRegistries(
    fromProviders(
      createCodecProviderIgnoreNone[Version],
      createCodecProvider[Candidate],
      createCodecProvider[Application]
    ),
    DEFAULT_CODEC_REGISTRY
  )

  lazy val mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:4.0.10"))

  lazy val mongoClient = MongoClient("mongodb://%s:%d".format(
    mongoDBContainer.getHost, mongoDBContainer.getFirstMappedPort))

  lazy val db = mongoClient.getDatabase("sdkman").withCodecRegistry(codecRegistry)

  lazy val appCollection: MongoCollection[Application] = db.getCollection("application")

  lazy val versionsCollection: MongoCollection[Version] = db.getCollection("versions")

  lazy val candidatesCollection: MongoCollection[Candidate] = db.getCollection("candidates")

  def startMongoDb() = {
    System.out.println("STARTING MONGOLO!")
    mongoDBContainer.start()
  }

  def stopMongoDb() = {
    System.out.println("STOPPING MONGOLO :-(")
//    mongoDBContainer.stop()
  }

  def getMongoDbHost() = {
    mongoDBContainer.getHost
  }

  def getMongoDbPort() = {
    mongoDBContainer.getFirstMappedPort
  }

  def insertApplication(app: Application) = appCollection.insertOne(app).results()

  def insertVersions(vs: Seq[Version]) = versionsCollection.insertMany(vs).results()

  def insertVersion(v: Version) = versionsCollection.insertOne(v).results()

  def insertCandidates(cs: Seq[Candidate]) = candidatesCollection.insertMany(cs).results()

  def insertCandidate(c: Candidate) = candidatesCollection.insertOne(c).results()

  def dropAllCollections() = {
    appCollection.drop().results()
    versionsCollection.drop().results()
    candidatesCollection.drop().results()
  }

  def isDefault(candidate: String, version: String): Boolean =
    Await.result(
      candidatesCollection
        .find(and(equal("candidate", candidate), equal("default", version)))
        .headOption()
        .map(_.nonEmpty),
      5.seconds
    )

  def hasDefault(candidate: String): Boolean =
    Await
      .result(
        candidatesCollection
          .find(equal("candidate", candidate))
          .first
          .toFuture,
        5.seconds
      )
      .default
      .nonEmpty

  def versionPublished(candidate: String, version: String, url: String, platform: String): Boolean =
    Await.result(
      versionsCollection
        .find(
          and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
        )
        .first
        .headOption()
        .map(_.nonEmpty),
      5.seconds
    )

  def findVersion(candidate: String, version: String, platform: String): Option[Version] =
    Await.result(
      versionsCollection
        .find(
          and(equal("candidate", candidate), equal("version", version), equal("platform", platform))
        )
        .first
        .headOption(),
      5.seconds
    )
}

object Helpers {

  implicit class DocumentObservable[C](val observable: Observable[Document])
      extends ImplicitObservable[Document] {
    override val converter: Document => String = doc => doc.toJson
  }

  implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {
    override val converter: C => String = doc => doc.toString
  }

  trait ImplicitObservable[C] {
    val observable: Observable[C]
    val converter: C => String

    def results(): Seq[C] = Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))

    def headResult() = Await.result(observable.head(), Duration(10, TimeUnit.SECONDS))

    def printResults(initial: String = ""): Unit = {
      if (initial.length > 0) print(initial)
      results().foreach(res => println(converter(res)))
    }

    def printHeadResult(initial: String = ""): Unit = println(s"$initial${converter(headResult())}")
  }

}
