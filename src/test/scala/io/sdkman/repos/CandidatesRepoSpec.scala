package io.sdkman.repos

import com.typesafe.config.{Config, ConfigFactory}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.model.Candidate
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, OptionValues}
import support.Mongo
import support.Mongo._

class CandidatesRepoSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with ScalaFutures
    with OptionValues {

  val scala = Candidate(
    "scala",
    "Scala",
    "The Scala Language",
    Some("2.12.0"),
    "http://www.scala-lang.org/",
    "UNIVERSAL"
  )
  val groovy = Candidate(
    "groovy",
    "Groovy",
    "The Groovy Language",
    Some("2.4.7"),
    "http://www.groovy-lang.org/",
    "UNIVERSAL"
  )
  val java = Candidate(
    "java",
    "Java",
    "The Java Language",
    Some("8u111"),
    "https://www.oracle.com",
    "MULTI_PLATFORM"
  )
  val micronaut = Candidate(
    "micronaut",
    "Micronaut",
    "The Micronaut framework",
    None,
    "http://micronaut.io/",
    "UNIVERSAL"
  )

  "candidates repository" should {

    "find all candidates regardless of distribution" in new TestRepo {
      whenReady(findAllCandidates()) { candidates =>
        candidates.size shouldBe 4
        candidates should contain(scala)
        candidates should contain(groovy)
        candidates should contain(java)
        candidates should contain(micronaut)
      }
    }

    "find candidates in alphabetically sorted order" in new TestRepo {
      whenReady(findAllCandidates()) { candidates =>
        candidates.size shouldBe 4
        candidates shouldBe Seq(groovy, java, micronaut, scala)
      }
    }

    "find some single candidate when searching by know candidate identifier" in new TestRepo {
      val candidate = "java"
      whenReady(findCandidate(candidate)) { maybeCandidate => maybeCandidate.value shouldBe java }
    }

    "find none when searching by unknown candidate identifier" in new TestRepo {
      val candidate = "scoobeedoo"
      whenReady(findCandidate(candidate)) { maybeCandidate => maybeCandidate shouldBe None }
    }

    "update a single candidate when present" in new TestRepo {
      val candidate = "scala"
      val version   = "2.12.1"
      whenReady(updateDefaultVersion(candidate, version)) { _ =>
        withClue(s"$candidate was not set to default $version") {
          isDefault(candidate, version) shouldBe true
        }
      }
    }

    "find a candidate with default version" in new TestRepo {
      val candidate = "scala"
      whenReady(findCandidate(candidate)) { maybeCandidate =>
        maybeCandidate.value.default.value shouldBe "2.12.0"
      }
    }

    "find a candidate with no default version" in new TestRepo {
      val candidate = "micronaut"
      whenReady(findCandidate(candidate)) { maybeCandidate =>
        maybeCandidate.value.default shouldBe None
      }
    }

    "insert a candidate with default version" in new TestRepo {
      whenReady(insertCandidate(scala)) { _ => hasDefault("scala") shouldBe true }
    }

    "insert a candidate with no default version" in new TestRepo {
      whenReady(insertCandidate(micronaut)) { _ => hasDefault("micronaut") shouldBe false }
    }
  }

  override def beforeAll() = Mongo.startMongoDb()

  before {
    Mongo.dropAllCollections()
    Mongo.insertCandidates(Seq(scala, groovy, java, micronaut))
  }

  private trait TestRepo extends CandidatesRepo with MongoConnectivity with MongoConfiguration {
    override val config: Config = ConfigFactory
      .parseString(s"""
           |mongo.url.host=${Mongo.getMongoDbHost()}
           |mongo.url.port=${Mongo.getMongoDbPort()}
      """.stripMargin)
      .withFallback(ConfigFactory.load())
  }
}
