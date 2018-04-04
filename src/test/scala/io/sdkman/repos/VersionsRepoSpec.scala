package io.sdkman.repos

import com.typesafe.config.{Config, ConfigFactory}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Matchers, OptionValues, WordSpec}
import support.Mongo
import support.Mongo.versionPublished

class VersionsRepoSpec extends WordSpec with Matchers with BeforeAndAfter with ScalaFutures with OptionValues {

  "versions repository" should {

    "persist a version" in new TestRepo {

      val candidate = "java"
      val version = "8u111"
      val platform = "LINUX_64"
      val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

      whenReady(saveVersion(Version(candidate, version, platform, url))) { completed =>
        completed.toString shouldBe "The operation completed successfully"
        versionPublished(candidate, version, url, platform) shouldBe true
      }
    }

    "attempt to find one Version by candidate, version and platform" when {

      "that version is available" in new TestRepo {
        val candidate = "java"
        val version = "8u111"
        val platform = "LINUX_64"
        val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

        Mongo.insertVersion(Version(candidate, version, platform, url))

        whenReady(findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.candidate shouldBe candidate
          maybeVersion.value.version shouldBe version
          maybeVersion.value.platform shouldBe platform
          maybeVersion.value.url shouldBe url
        }
      }

      "find no Version by candidate, version and platform" in new TestRepo {
        whenReady(findVersion("java", "7u65", "LINUX_64")) { maybeVersion =>
          maybeVersion should not be defined
        }
      }
    }

    "attempt to find all versions by candidate and platform ordered by version" in new TestRepo {
      val java8u111 = Version("java", "8u111", "LINUX_64", "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz")
      val java8u121 = Version("java", "8u121", "LINUX_64", "http://dl/8u121-b14/jdk-8u121-linux-x64.tar.gz")
      val java8u131 = Version("java", "8u131", "LINUX_64", "http://dl/8u131-b14/jdk-8u131-linux-x64.tar.gz")

      val javaVersions = Seq(java8u111, java8u121, java8u131)

      javaVersions.foreach(Mongo.insertVersion)

      whenReady(findAllVersionsByCandidatePlatform("java", "LINUX_64")) { versions =>
        versions.size shouldBe 3
        versions(0) shouldBe java8u111
        versions(1) shouldBe java8u121
        versions(2) shouldBe java8u131
      }
    }

    "attempt to find all Versions by candidate and version" when {

      "more than one version platform is available" in new TestRepo {

        val candidate = "java"
        val version = "8u111"
        val url = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

        Mongo.insertVersion(Version(candidate, version, "LINUX_64", url))
        Mongo.insertVersion(Version(candidate, version, "MAC_OSX", url))

        whenReady(findAllVersionsByCandidateVersion(candidate, version)) { versions =>
          versions shouldBe 'nonEmpty
          versions.size shouldBe 2
        }
      }
    }
  }

  before {
    Mongo.dropAllCollections()
  }

  private trait TestRepo extends VersionsRepo with MongoConnectivity with MongoConfiguration {
    override val config: Config = ConfigFactory.load()
  }

}
