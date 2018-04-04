package io.sdkman.db

import com.mongodb.ConnectionString
import org.mongodb.scala.connection.ClusterSettings
import org.mongodb.scala.{MongoClient, MongoClientSettings, MongoCredential}

trait MongoConnectivity {

  self: MongoConfiguration =>

  def credential = MongoCredential.createCredential(userName, databaseName, password.toCharArray)

  lazy val clusterSettings = ClusterSettings.builder()
    .applyConnectionString(new ConnectionString(mongoUrl))
    .build()

  lazy val clientSettings = MongoClientSettings.builder()
    .credential(credential)
    .clusterSettings(clusterSettings)
    .build()

  lazy val mongoClient = if (userName.isEmpty) MongoClient(mongoUrl) else MongoClient(clientSettings)

  def db = mongoClient.getDatabase(databaseName)

  def appCollection = db.getCollection("application")

  def versionsCollection = db.getCollection("versions")

  def candidatesCollection = db.getCollection("candidates")
}

