package io.sdkman.repos

import com.typesafe.config.{Config, ConfigFactory}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, OptionValues}
import support.Mongo
import support.Mongo.versionPublished

class VersionsRepoSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfter
    with ScalaFutures
    with OptionValues
    with TableDrivenPropertyChecks
    with IntegrationPatience {

  "versions repository" should {

    "persist a version" in new TestRepo {

      val candidate = "java"
      val version   = "8u111"
      val platform  = "LINUX_64"
      val url       = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

      whenReady(saveVersion(Version(candidate, version, platform, url))) { completed =>
        completed.toString shouldBe "The operation completed successfully"
        versionPublished(candidate, version, url, platform) shouldBe true
      }
    }

    "update a version" in new TestRepo {

      val candidate = "java"
      val version   = "8u111"
      val platform  = "LINUX_64"

      private val original = Version(
        candidate,
        version,
        platform,
        "http://dl/8u101-b13/jdk-8u101-linux-x64.tar.gz",
        Some("oracle"),
        Some(false)
      )
      private val updated = Version(
        candidate,
        version,
        platform,
        "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz",
        Some("zulu"),
        Some(true)
      )

      whenReady(saveVersion(original).flatMap(_ => updateVersion(original, updated))) { result =>
        result.getModifiedCount shouldBe 1
        Mongo.findVersion(candidate, version, platform).value shouldBe updated
      }
    }

    "delete a version" in new TestRepo {
      val candidate = "java"
      val version   = "8u111"
      val platform  = "LINUX_64"

      private val original = Version(
        candidate,
        version,
        platform,
        "http://dl/8u101-b13/jdk-8u101-linux-x64.tar.gz",
        Some("oracle"),
        Some(false)
      )

      whenReady(saveVersion(original).flatMap(_ => deleteVersion(original))) { result =>
        result.getDeletedCount shouldBe 1
        Mongo.findVersion(candidate, version, platform) shouldBe None
      }
    }

    "attempt to find one Version by candidate, version and platform" when {

      "that version is available" in new TestRepo {
        val candidate = "java"
        val version   = "8u111"
        val platform  = "LINUX_64"
        val url       = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

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
      val java8u111 =
        Version("java", "8u111", "LINUX_64", "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz")
      val java8u121 =
        Version("java", "8u121", "LINUX_64", "http://dl/8u121-b14/jdk-8u121-linux-x64.tar.gz")
      val java8u131 =
        Version("java", "8u131", "LINUX_64", "http://dl/8u131-b14/jdk-8u131-linux-x64.tar.gz")

      val javaVersions = Seq(java8u111, java8u121, java8u131)

      javaVersions.foreach(Mongo.insertVersion)

      whenReady(findAllVisibleVersionsByCandidatePlatform("java", "LINUX_64")) { versions =>
        versions.size shouldBe 3
        versions(0) shouldBe java8u111
        versions(1) shouldBe java8u121
        versions(2) shouldBe java8u131
      }
    }

    "attempt to find all versions by candidate and mixed platform ordered by version" in new TestRepo {
      val mn2linux     = Version("micronaut", "2.0.0", "LINUX_64", "http://dl/mn-2.0.0-linux.tar.gz")
      val mn2darwin    = Version("micronaut", "2.0.0", "MAC_OSX", "http://dl/mn-2.0.0-darwin.tar.gz")
      val mn2windows   = Version("micronaut", "2.0.0", "WINDOWS_64", "http://dl/mn-2.0.0-windows.zip")
      val mn1universal = Version("micronaut", "1.3.5", "UNIVERSAL", "http://dl/mn-1.3.5.zip")

      val micronautVersions = Seq(mn1universal, mn2darwin, mn2linux, mn2windows)

      micronautVersions.foreach(Mongo.insertVersion)

      whenReady(findAllVersionsByCandidatePlatform("micronaut", "LINUX_64")) { versions =>
        versions.size shouldBe 2
        versions(0) shouldBe mn1universal
        versions(1) shouldBe mn2linux
      }
    }

    "attempt to find all versions by candidate and platform" when {

      "when they are set to be visible" in new TestRepo {
        val java8u111 = Version(
          "java",
          "8u111",
          "LINUX_64",
          "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz",
          visible = Some(false)
        )
        val java8u121 = Version(
          "java",
          "8u121",
          "LINUX_64",
          "http://dl/8u121-b14/jdk-8u121-linux-x64.tar.gz",
          visible = Some(true)
        )

        val javaVersions = Seq(java8u111, java8u121)

        javaVersions.foreach(Mongo.insertVersion)

        whenReady(findAllVisibleVersionsByCandidatePlatform("java", "LINUX_64")) { versions =>
          versions.size shouldBe 1
          versions(0) shouldBe java8u121
        }
      }

      "when they are set to be visible by their default value" in new TestRepo {
        val java8u111 = Version(
          "java",
          "8u111",
          "LINUX_64",
          "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz",
          visible = Some(false)
        )
        val java8u121 =
          Version("java", "8u121", "LINUX_64", "http://dl/8u121-b14/jdk-8u121-linux-x64.tar.gz")

        val javaVersions = Seq(java8u111, java8u121)

        javaVersions.foreach(Mongo.insertVersion)

        whenReady(findAllVisibleVersionsByCandidatePlatform("java", "LINUX_64")) { versions =>
          versions.size shouldBe 1
          versions(0) shouldBe java8u121
        }
      }

      "when they have no `visible` field" in new TestRepo {
        val java8u111 = Version(
          "java",
          "8u111",
          "LINUX_64",
          "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz",
          visible = Some(false)
        )
        val java8u121 = Version(
          "java",
          "8u121",
          "LINUX_64",
          "http://dl/8u121-b14/jdk-8u121-linux-x64.tar.gz",
          visible = None
        )

        val javaVersions = Seq(java8u111, java8u121)

        javaVersions.foreach(Mongo.insertVersion)

        whenReady(findAllVisibleVersionsByCandidatePlatform("java", "LINUX_64")) { versions =>
          versions.size shouldBe 1
          versions(0) shouldBe java8u121
        }
      }
    }

    "read versions with optional vendor" when {

      val candidate = "java"
      val version   = "8.0.131"
      val platform  = "LINUX_64"

      "a vendor is present" in new TestRepo with OptionValues {
        Mongo.insertVersion(
          Version(
            candidate,
            version,
            platform,
            "http://dl/8u131-b14/jdk-8u131-linux-x64.tar.gz",
            Some("amazon")
          )
        )
        whenReady(findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.vendor.value shouldBe "amazon"
        }
      }

      "a vendor is not present" in new TestRepo {
        Mongo.insertVersion(
          Version(
            candidate,
            version,
            platform,
            "http://dl/8u131-b14/jdk-8u131-linux-x64.tar.gz",
            None
          )
        )
        whenReady(findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.vendor should not be 'defined
        }
      }
    }

    "read version with optional visibility" when {
      val candidate = "java"
      val version   = "8.0.265.hs"
      val platform  = "LINUX_64"
      val url       = "https://dl/OpenJDK8U-jdk_x64_linux_hotspot_8u272b10.tar.gz"

      "the version without `visible` field is visible" in new TestRepo {
        Mongo.insertVersion(Version(candidate, version, platform, url, Some("adpt"), None))

        whenReady(findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.visible should not be 'defined
        }
      }

      "the version is visible" in new TestRepo {
        Mongo.insertVersion(Version(candidate, version, platform, url, Some("adpt"), Some(true)))

        whenReady(findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.visible.value shouldBe true
        }
      }

      "the version is not visible" in new TestRepo {
        Mongo.insertVersion(Version(candidate, version, platform, url, Some("adpt"), Some(false)))

        whenReady(findVersion(candidate, version, platform)) { maybeVersion =>
          maybeVersion.value.visible.value shouldBe false
        }
      }
    }

    "attempt to find all Versions by candidate and version" when {

      "more than one version platform is available" in new TestRepo {

        val candidate = "java"
        val version   = "8u111"
        val url       = "http://dl/8u111-b14/jdk-8u111-linux-x64.tar.gz"

        Mongo.insertVersion(Version(candidate, version, "LINUX_64", url))
        Mongo.insertVersion(Version(candidate, version, "MAC_OSX", url))

        whenReady(findAllVersionsByCandidateVersion(candidate, version)) { versions =>
          versions shouldBe 'nonEmpty
          versions.size shouldBe 2
        }
      }
    }

    "attempt to find all versions by platform where version starts with a major version and ends on a vendor suffix" in new TestRepo {
      val data =
        Table(
          ("platform", "majorVersion", "vendorSuffix", "installedVersions", "expectedVersions"),
          ("LINUX_64", 8, "open", Seq.empty, Seq.empty),
          (
            "LINUX_64",
            8,
            "open",
            Seq(
              Version(
                "java",
                "8.0.265-open",
                "LINUX_64",
                "https://dl/OpenJDK8U-jdk_x64_linux_8u265b01.tar.gz",
                Some("open")
              )
            ),
            Seq(
              Version(
                "java",
                "8.0.265-open",
                "LINUX_64",
                "https://dl/OpenJDK8U-jdk_x64_linux_8u265b01.tar.gz",
                Some("open")
              )
            )
          ),
          (
            "LINUX_64",
            8,
            "open",
            Seq(
              Version(
                "java",
                "8.0.265-open",
                "LINUX_64",
                "https://dl/OpenJDK8U-jdk_x64_linux_8u265b01.tar.gz",
                Some("open")
              ),
              Version(
                "java",
                "11.0.2-open",
                "LINUX_64",
                "https://dl/openjdk-11.0.2_linux-x64_bin.tar.gz",
                Some("open")
              )
            ),
            Seq(
              Version(
                "java",
                "8.0.265-open",
                "LINUX_64",
                "https://dl/OpenJDK8U-jdk_x64_linux_8u265b01.tar.gz",
                Some("open")
              )
            )
          ),
          (
            "LINUX_64",
            16,
            "open",
            Seq(
              Version(
                "java",
                "16.ea.28-open",
                "LINUX_64",
                "https://dl/openjdk-16-ea+28_linux-x64_bin.tar.gz",
                Some("open")
              ),
              Version(
                "java",
                "16.ea.9.lm-open",
                "LINUX_64",
                "https://dl/openjdk-16-loom+9-316_linux-x64_bin.tar.gz",
                Some("open")
              )
            ),
            Seq(
              Version(
                "java",
                "16.ea.28-open",
                "LINUX_64",
                "https://dl/openjdk-16-ea+28_linux-x64_bin.tar.gz",
                Some("open")
              )
            )
          ),
          (
            "LINUX_64",
            13,
            "librca",
            Seq(
              Version(
                "java",
                "13.0.2-librca",
                "LINUX_64",
                "https://dl/bellsoft-jdk13.0.2+9-linux-amd64.tar.gz",
                Some("librca")
              ),
              Version(
                "java",
                "13.0.2.fx-librca",
                "LINUX_64",
                "https://dl/bellsoft-jdk13.0.2+9-linux-amd64-full.tar.gz",
                Some("librca")
              )
            ),
            Seq(
              Version(
                "java",
                "13.0.2-librca",
                "LINUX_64",
                "https://dl/bellsoft-jdk13.0.2+9-linux-amd64.tar.gz",
                Some("librca")
              )
            )
          ),
          (
            "LINUX_64",
            13,
            "fx-librca",
            Seq(
              Version(
                "java",
                "13.0.2-librca",
                "LINUX_64",
                "https://dl/bellsoft-jdk13.0.2+9-linux-amd64.tar.gz",
                Some("librca")
              ),
              Version(
                "java",
                "13.0.2.fx-librca",
                "LINUX_64",
                "https://dl/bellsoft-jdk13.0.2+9-linux-amd64-full.tar.gz",
                Some("librca")
              )
            ),
            Seq(
              Version(
                "java",
                "13.0.2.fx-librca",
                "LINUX_64",
                "https://dl/bellsoft-jdk13.0.2+9-linux-amd64-full.tar.gz",
                Some("librca")
              )
            )
          )
        )

      forAll(data) {
        (
            platform: String,
            majorVersion: Int,
            vendorSuffix: String,
            installedVersions: Seq[Version],
            expectedVersions: Seq[Version]
        ) =>
          Mongo.dropAllCollections()

          installedVersions.foreach(Mongo.insertVersion)

          whenReady(findJavaVersionSeries(platform, majorVersion, vendorSuffix)) { versions =>
            versions shouldBe expectedVersions
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
