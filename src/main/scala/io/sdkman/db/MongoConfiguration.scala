package io.sdkman.db

import com.typesafe.config.Config

trait MongoConfiguration {

  def config: Config

  lazy val mongoUrl = config.getString("mongo.url")

  lazy val userName = config.getString("mongo.username")

  lazy val password = config.getString("mongo.password")

  lazy val databaseName = config.getString("mongo.database")
}
