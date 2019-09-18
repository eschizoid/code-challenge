package com.otus.codechallenge.graphql

import com.otus.codechallenge.graphql.SchemaDefinition.schema
import com.otus.codechallenge.mongo.MongoConnection
import com.otus.codechallenge.repository.{ClassMongoRepository, StudentMongoRepository}
import com.otus.codechallenge.service.GraphqlStudentsFetcher
import io.circe.parser._
import org.scalatest.{FlatSpec, Matchers}
import sangria.ast.Document
import sangria.execution.Executor
import sangria.macros._
import sangria.marshalling.circe._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SchemaDefinitionSpec extends FlatSpec with Matchers {

  val client            = MongoConnection("mongodb", 27017, "mongo", "otus", "mongo")
  val classRepository   = ClassMongoRepository(client)
  val studentRepository = StudentMongoRepository(client)

  "A valid graphql query" should "find students information by first name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)
    val query =
      graphql"""
         query FindByFirstName {
           SearchStudentDetails(firstName: "Mike") {
            firstName
            lastName
            gpa
           }
         }
       """

    val expectedResult =
      """
        |{
        |  "data" : {
        |    "SearchStudentDetails" : [
        |      {
        |        "firstName" : "Mike",
        |        "lastName" : "Williams",
        |        "gpa" : 2.6666666666666665
        |      }
        |    ]
        |  }
        |}
        |""".stripMargin

    // Exercise
    val result = executeQuery(query, fetcher = fetcher)

    // Assert
    result shouldBe parse(expectedResult).right.get
  }

  "A valid graphql query" should "find students information by last name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)
    val query =
      graphql"""
         query FindByLastName {
           SearchStudentDetails(lastName: "Smith") {
            firstName
            lastName
            gpa
           }
         }
       """

    val expectedResult =
      """
        |{
        |  "data" : {
        |    "SearchStudentDetails" : [
        |      {
        |        "firstName" : "John",
        |        "lastName" : "Smith",
        |        "gpa" : 3.1666666666666665
        |      },
        |      {
        |        "firstName" : "Jane",
        |        "lastName" : "Smith",
        |        "gpa" : 2.75
        |      }
        |    ]
        |  }
        |}
        |""".stripMargin

    // Exercise
    val result = executeQuery(query, fetcher = fetcher)

    // Assert
    result shouldBe parse(expectedResult).right.get
  }

  "A valid graphql query" should "find students information by first and last name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)
    val query =
      graphql"""
         query FindByFirstNameAndLastName {
           SearchStudentDetails(firstName: "Samantha", lastName: "Ware") {
            firstName
            lastName
            gpa
           }
         }
       """

    val expectedResult =
      """
        |{
        |  "data" : {
        |    "SearchStudentDetails" : [
        |      {
        |        "firstName" : "Samantha",
        |        "lastName" : "Ware",
        |        "gpa" : 3.75
        |      }
        |    ]
        |  }
        |}
        |""".stripMargin

    // Exercise
    val result = executeQuery(query, fetcher = fetcher)

    // Assert
    result shouldBe parse(expectedResult).right.get
  }

  "A valid graphql query" should "find students detailed information by first name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)
    val query =
      graphql"""
         query FindByFirstName {
           SearchStudentDetails(firstName: "Mike") {
            firstName
            lastName
            gpa
            emailAddress
            studentClasses {
              id
              name
              grade
             }
           }
         }
       """
    val expectedResult =
      """{
        |    "data" : {
        |      "SearchStudentDetails" : [
        |        {
        |          "firstName" : "Mike",
        |          "lastName" : "Williams",
        |          "gpa" : 2.6666666666666665,
        |          "emailAddress" : "mikewilliams@mailinator.com",
        |          "studentClasses" : [
        |            {
        |              "id" : "4",
        |              "name" : "Social Studies 101",
        |              "grade" : 2.0
        |            },
        |            {
        |              "id" : "5",
        |              "name" : "Health 101",
        |              "grade" : 3.0
        |            },
        |            {
        |              "id" : "6",
        |              "name" : "Chemistry 101",
        |              "grade" : 4.0
        |            },
        |            {
        |              "id" : "7",
        |              "name" : "English 201",
        |              "grade" : 2.0
        |            },
        |            {
        |              "id" : "8",
        |              "name" : "Math 201",
        |              "grade" : 1.5
        |            },
        |            {
        |              "id" : "1",
        |              "name" : "Math 101",
        |              "grade" : 3.5
        |            }
        |          ]
        |        }
        |      ]
        |    }
        |  }""".stripMargin

    // Exercise
    val result = executeQuery(query, fetcher = fetcher)

    // Assert
    result shouldBe parse(expectedResult).right.get
  }

  "A valid graphql query" should "find students detailed information by last name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)
    val query =
      graphql"""
         query FindByFirstName {
           SearchStudentDetails(lastName: "Smith") {
            firstName
            lastName
            gpa
            emailAddress
            studentClasses {
              id
              name
              grade
             }
           }
         }
       """
    val expectedResult =
      """{
        |    "data" : {
        |      "SearchStudentDetails" : [
        |        {
        |          "firstName" : "John",
        |          "lastName" : "Smith",
        |          "gpa" : 3.1666666666666665,
        |          "emailAddress" : "johnsmith@mailinator.com",
        |          "studentClasses" : [
        |            {
        |              "id" : "1",
        |              "name" : "Math 101",
        |              "grade" : 4.0
        |            },
        |            {
        |              "id" : "2",
        |              "name" : "English 101",
        |              "grade" : 3.0
        |            },
        |            {
        |              "id" : "3",
        |              "name" : "Science 101",
        |              "grade" : 2.0
        |            },
        |            {
        |              "id" : "4",
        |              "name" : "Social Studies 101",
        |              "grade" : 2.5
        |            },
        |            {
        |              "id" : "5",
        |              "name" : "Health 101",
        |              "grade" : 3.5
        |            },
        |            {
        |              "id" : "6",
        |              "name" : "Chemistry 101",
        |              "grade" : 4.0
        |            }
        |          ]
        |        },
        |        {
        |          "firstName" : "Jane",
        |          "lastName" : "Smith",
        |          "gpa" : 2.75,
        |          "emailAddress" : "janesmith@mailinator.com",
        |          "studentClasses" : [
        |            {
        |              "id" : "1",
        |              "name" : "Math 101",
        |              "grade" : 3.0
        |            },
        |            {
        |              "id" : "2",
        |              "name" : "English 101",
        |              "grade" : 2.0
        |            },
        |            {
        |              "id" : "3",
        |              "name" : "Science 101",
        |              "grade" : 2.5
        |            },
        |            {
        |              "id" : "5",
        |              "name" : "Health 101",
        |              "grade" : 3.5
        |            },
        |            {
        |              "id" : "6",
        |              "name" : "Chemistry 101",
        |              "grade" : 2.0
        |            },
        |            {
        |              "id" : "7",
        |              "name" : "English 201",
        |              "grade" : 3.5
        |            }
        |          ]
        |        }
        |      ]
        |    }
        |  }""".stripMargin

    // Exercise
    val result = executeQuery(query, fetcher = fetcher)

    // Assert
    result shouldBe parse(expectedResult).right.get
  }

  "A valid graphql query" should "find students detailed information by first and last name" in {
    // Prepare
    val fetcher = GraphqlStudentsFetcher(studentRepository, classRepository)
    val query =
      graphql"""
         query FindByFirstNameAndLastName {
           SearchStudentDetails(firstName: "Samantha", lastName: "Ware") {
            firstName
            lastName
            gpa
            emailAddress
            studentClasses {
              id
              name
              grade
             }
           }
         }
       """

    val expectedResult =
      """{
        |    "data" : {
        |      "SearchStudentDetails" : [
        |        {
        |          "firstName" : "Samantha",
        |          "lastName" : "Ware",
        |          "gpa" : 3.75,
        |          "emailAddress" : "sware@mailinator.com",
        |          "studentClasses" : [
        |            {
        |              "id" : "1",
        |              "name" : "Math 101",
        |              "grade" : 4.0
        |            },
        |            {
        |              "id" : "3",
        |              "name" : "Science 101",
        |              "grade" : 4.0
        |            },
        |            {
        |              "id" : "4",
        |              "name" : "Social Studies 101",
        |              "grade" : 4.0
        |            },
        |            {
        |              "id" : "5",
        |              "name" : "Health 101",
        |              "grade" : 3.5
        |            },
        |            {
        |              "id" : "6",
        |              "name" : "Chemistry 101",
        |              "grade" : 3.0
        |            },
        |            {
        |              "id" : "8",
        |              "name" : "Math 201",
        |              "grade" : 4.0
        |            }
        |          ]
        |        }
        |      ]
        |    }
        |  }""".stripMargin

    // Exercise
    val result = executeQuery(query, fetcher = fetcher)

    // Assert
    result shouldBe parse(expectedResult).right.get
  }

  private def executeQuery(query: Document, fetcher: GraphqlStudentsFetcher) = {
    val futureResult = Executor.execute(schema, query, fetcher)
    Await.result(futureResult, 15.seconds)
  }
}
