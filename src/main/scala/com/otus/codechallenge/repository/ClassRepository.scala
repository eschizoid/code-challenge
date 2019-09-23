package com.otus.codechallenge.repository

import com.otus.codechallenge.mongo.Classes
import com.otus.codechallenge.util.Logging
import org.mongodb.scala.model.Projections.{fields, include}
import org.mongodb.scala.{MongoCollection, MongoDatabase}

import scala.concurrent.Future
import scala.language.reflectiveCalls

trait ClassRepository {

  /**
    * Fetches the classes catalogue
    * @return the classes catalogue
    */
  def fetchAll(): Future[Seq[Classes]]
}

/**
  * Repository class for returning Class entities.
  * @param client the mongo client
  */
class ClassMongoRepository(client: MongoDatabase) extends ClassRepository with Logging {

  val collection: MongoCollection[Classes] = client.getCollection("students")

  override def fetchAll(): Future[Seq[Classes]] = {
    logger.debug(f"Fetching class catalogue")
    collection.find().projection(fields(include("classes"))).toFuture()
  }
}

object ClassMongoRepository {
  def apply(client: MongoDatabase): ClassMongoRepository = {
    new ClassMongoRepository(client: MongoDatabase)
  }
}
