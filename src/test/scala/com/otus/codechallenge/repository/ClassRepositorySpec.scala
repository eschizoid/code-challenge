package com.otus.codechallenge.repository

import com.otus.codechallenge.mongo.MongoConnection
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class ClassRepositorySpec extends FlatSpec with Matchers {
  val client = MongoConnection("mongodb": String, 27017, "mongo", "otus", "mongo")

  "Class Repository should" should "retrieve all classes" in {
    // Prepare
    val classMongoRepository = ClassMongoRepository(client)

    // Exercise
    val classCatalogue = Await.result(classMongoRepository.fetchAll(), 5 seconds)

    // Assert
    val catalogue = classCatalogue.head
    catalogue.classes should have size 8
    catalogue.classes("1") shouldEqual "Math 101"
  }
}
