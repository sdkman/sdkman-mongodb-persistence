package io.sdkman.db

import com.typesafe.config.Config

trait MongoConfiguration {

  def config: Config

  lazy val mongoHost = config.getString("mongo.url.host")

  lazy val mongoPort = config.getInt("mongo.url.port")

  lazy val userName = config.getString("mongo.username")

  lazy val password = config.getString("mongo.password")

  lazy val databaseName = config.getString("mongo.database")
}
