package com.otus.codechallenge.service

import com.otus.codechallenge.mongo.MongoConnection
import com.otus.codechallenge.repository.{ClassMongoRepository, StudentMongoRepository}
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class GraphqlFetcherSpec extends FlatSpec with Matchers {
  val client            = MongoConnection("mongodb", 27017, "mongo", "otus", "mongo")
  val classRepository   = ClassMongoRepository(client)
  val studentRepository = StudentMongoRepository(client)

  "Graphql fetcher" should "find student detailed information by first name and last name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)

    // Exercise
    val student = Await.result(fetcher.searchAndTransformStudents(Some("Samantha"), Some("Ware")), 10 seconds).head

    // Assert
    student.firstName shouldBe "Samantha"
    student.lastName shouldBe "Ware"
    student.gpa shouldBe 3.75
    student.emailAddress shouldBe "sware@mailinator.com"
    student.studentClasses should have size 6
  }

  "Graphql fetcher" should "find student information by first name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)

    // Exercise
    val student = Await.result(fetcher.searchAndTransformStudents(Some("Mike"), None), 10 seconds).head

    // Assert
    student should not be None
  }

  "Graphql fetcher" should "find students detailed information by last name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)

    // Exercise
    val student = Await.result(fetcher.searchAndTransformStudents(None, Some("Smith")), 10 seconds)

    // Assert
    student should have size 2
  }
}
