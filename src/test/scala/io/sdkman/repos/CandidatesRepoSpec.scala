package io.sdkman.repos

import com.typesafe.config.ConfigFactory
import io.sdkman.db.MongoConnectivity
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import support.Mongo

class CandidatesRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures {

  val scala = Candidate("scala", "Scala", "The Scala Language", "2.12.0", "http://www.scala-lang.org/", "UNIVERSAL")
  val groovy = Candidate("groovy", "Groovy", "The Groovy Language", "2.4.7", "http://www.groovy-lang.org/", "UNIVERSAL")
  val java = Candidate("java", "Java", "The Java Language", "8u111", "https://www.oracle.com", "MULTI_PLATFORM")

  "candidates repository" should {

    "find all candidates regardless of distribution" in new TestRepo {
      whenReady(findAllCandidates()) { candidates =>
        candidates.size shouldBe 3
        candidates should contain(scala)
        candidates should contain(groovy)
        candidates should contain(java)
      }
    }

    "find candidates in alphabetically sorted order" in new TestRepo {
      whenReady(findAllCandidates()) { candidates =>
        candidates.size shouldBe 3
        candidates(0) shouldBe groovy
        candidates(1) shouldBe java
        candidates(2) shouldBe scala
      }
    }

    "find some single candidate when searching by know candidate identifier" in new TestRepo {
      val candidate = "java"
      whenReady(findByIdentifier(candidate)) { maybeCandidate =>
        maybeCandidate shouldBe defined
        maybeCandidate.foreach(_.candidate shouldBe candidate)
      }
    }

    "find none when searching by unknown candidate identifier" in new TestRepo {
      val candidate = "scoobeedoo"
      whenReady(findByIdentifier(candidate)) { maybeCandidate =>
        maybeCandidate shouldNot be(defined)
      }
    }
  }

  before {
    Mongo.dropAllCollections()
    Mongo.insertCandidates(Seq(scala, groovy, java))
  }

  sealed trait TestRepo extends CandidatesRepo with MongoConnectivity {
    override val config = ConfigFactory.load()
  }
}
