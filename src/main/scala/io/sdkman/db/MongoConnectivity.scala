package io.sdkman.db

import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCredential, ServerAddress}

import scala.collection.JavaConverters._

trait MongoConnectivity {

  self: MongoConfiguration =>

  def credential = MongoCredential.createCredential(userName, databaseName, password.toCharArray)

  lazy val clientSettings = MongoClientSettings.builder()
    .credential(credential)
    .applyToClusterSettings(b => b.hosts(List(ServerAddress(mongoUrl)).asJava))
    .build()

  lazy val mongoClient: MongoClient = if (userName.isEmpty) MongoClient(mongoUrl) else MongoClient(clientSettings)

  def db = mongoClient.getDatabase(databaseName)

  def appCollection = db.getCollection("application")

  def versionsCollection = db.getCollection("versions")

  def candidatesCollection = db.getCollection("candidates")
}

