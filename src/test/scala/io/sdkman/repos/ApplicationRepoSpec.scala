package io.sdkman.repos

import com.typesafe.config.{Config, ConfigFactory}
import io.sdkman.db.{MongoConfiguration, MongoConnectivity}
import io.sdkman.model.Application
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, OptionValues}
import support.Mongo

class ApplicationRepoSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfter
    with BeforeAndAfterAll
    with ScalaFutures
    with OptionValues {

  "application repository" should {

    "attempt to find a single Application" when {

      "that row is available" in new TestRepo {

        val alive                  = "OK"
        val stableCliVersion       = "8.8.8+888"
        val stableNativeCliVersion = "0.1.0"
        val betaCliVersion         = "9.9.9+999"
        val betaNativeCliVersion   = "0.2.0"

        Mongo.insertApplication(
          Application(
            alive = alive,
            stableCliVersion = stableCliVersion,
            stableNativeCliVersion = stableNativeCliVersion,
            betaCliVersion = betaCliVersion,
            betaNativeCliVersion = betaNativeCliVersion
          )
        )

        whenReady(findApplication()) { maybeApp =>
          maybeApp.value.alive shouldBe alive
          maybeApp.value.stableCliVersion shouldBe stableCliVersion
          maybeApp.value.betaCliVersion shouldBe betaCliVersion
          maybeApp.value.stableNativeCliVersion shouldBe stableNativeCliVersion
          maybeApp.value.betaNativeCliVersion shouldBe betaNativeCliVersion
        }
      }

      "that row is missing" in new TestRepo {
        whenReady(findApplication()) { maybeApp => maybeApp shouldBe None }
      }
    }
  }

  override def beforeAll() = Mongo.startMongoDb()

  before {
    Mongo.dropAllCollections()
  }

  private trait TestRepo extends ApplicationRepo with MongoConnectivity with MongoConfiguration {
    override val config: Config = ConfigFactory
      .parseString(s"""
            |mongo.url.host=${Mongo.getMongoDbHost()}
            |mongo.url.port=${Mongo.getMongoDbPort()}
      """.stripMargin)
      .withFallback(ConfigFactory.load())
  }
}
