package io.sdkman.db

import com.mongodb.{ConnectionString, MongoClientSettings}
import io.sdkman.repos.{Application, Candidate, Version}
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoCollection, MongoDatabase}

trait MongoConnectivity {

  self: MongoConfiguration =>

  def remoteConnectionString =
    new ConnectionString(s"mongodb://$userName:$password@$mongoHost:$mongoPort/$databaseName?authMechanism=SCRAM-SHA-1")

  lazy val clientSettings = MongoClientSettings.builder()
    .applyConnectionString(remoteConnectionString)
    .codecRegistry(MongoClient.DEFAULT_CODEC_REGISTRY)
    .build()

  lazy val localConnectionString = s"mongodb://localhost:27017/$databaseName"

  lazy val mongoClient: MongoClient =
    if (userName.isEmpty)
      MongoClient(localConnectionString)
    else MongoClient(clientSettings)

  val codecRegistry = fromRegistries(
    fromProviders(classOf[Version], classOf[Candidate], classOf[Application]),
    DEFAULT_CODEC_REGISTRY)

  def db: MongoDatabase = mongoClient.getDatabase(databaseName).withCodecRegistry(codecRegistry)

  def appCollection: MongoCollection[Application] = db.getCollection("application")

  def versionsCollection: MongoCollection[Version] = db.getCollection("versions")

  def candidatesCollection: MongoCollection[Candidate] = db.getCollection("candidates")

}
