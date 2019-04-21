package io.sdkman.db

import com.mongodb.{ConnectionString, MongoClientSettings}
import org.mongodb.scala.MongoClient

trait MongoConnectivity {

  self: MongoConfiguration =>

  def remoteConnectionString =
    new ConnectionString(s"mongodb://$userName:$password@$mongoHost:$mongoPort/$databaseName?authMechanism=SCRAM-SHA-1")

  lazy val clientSettings = MongoClientSettings.builder()
    .applyConnectionString(remoteConnectionString)
    .build()

  lazy val localConnectionString = s"mongodb://localhost:27017/$databaseName"

  lazy val mongoClient: MongoClient =
    if (userName.isEmpty)
      MongoClient(localConnectionString)
    else MongoClient(clientSettings)

  def db = mongoClient.getDatabase(databaseName)

  def appCollection = db.getCollection("application")

  def versionsCollection = db.getCollection("versions")

  def candidatesCollection = db.getCollection("candidates")
}
