package com.otus.codechallenge.mongo

import com.otus.codechallenge.util.Logging
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.mongodb.scala.bson.codecs.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros._
import org.mongodb.scala.{MongoClient, MongoDatabase}

import scala.util.{Failure, Success, Try}

case class MongoConnection()

/**
  * Singleton object for sharing the mongo connection across the different mongo repositories.
  */
object MongoConnection extends Logging {
  private lazy val codecRegistry =
    fromRegistries(fromProviders(classOf[Student], classOf[StudentClasses], classOf[Students], classOf[Classes]), DEFAULT_CODEC_REGISTRY)

  def apply(host: String, port: Integer, username: String, database: String, password: String): MongoDatabase = {
    logger.info("Acquiring mongo connection")
    val uri: String = f"mongodb://${username}:${password}@${host}:${port}/${database}?authSource=admin"
    val client      = MongoClient(uri)
    Try(client.getDatabase(database).withCodecRegistry(codecRegistry)) match {
      case Success(value) => value
      case Failure(e) =>
        logger.error(f"Unable to acquire mongo connection: $e")
        throw new RuntimeException("Unable to acquire mongo connection")
    }
  }
}
