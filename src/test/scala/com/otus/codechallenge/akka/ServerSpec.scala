package com.otus.codechallenge.akka

import io.circe.Printer
import io.circe.parser._
import okhttp3.{HttpUrl, OkHttpClient, Request, RequestBody}
import org.scalatest.{FlatSpec, Matchers}

class ServerSpec extends FlatSpec with Matchers {

  val client  = new OkHttpClient
  val printer = Printer.noSpaces
  val baseApiUrl: HttpUrl = new HttpUrl.Builder()
    .scheme("http")
    .host("graphql-api")
    .port(8080)
    .build()

  "GraphQL server" should "found students details when the last name is present in database" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/graphql"))
      .post(
        RequestBody.create(
          """
            |query FindByFirstNameAndLastName {
            |  SearchStudentDetails(lastName: "Smith") {
            |    firstName
            |    lastName
            |    studentClasses {
            |      id
            |      name
            |      grade
            |    }
            |  }
            |}
            |""".stripMargin.getBytes()
        )
      )
      .build()

    val expectedJson = """{
                         |  "data": {
                         |    "SearchStudentDetails": [
                         |      {
                         |        "firstName": "John",
                         |        "lastName": "Smith",
                         |        "studentClasses": [
                         |          {
                         |            "id": "1",
                         |            "name": "Math 101",
                         |            "grade": 4.0
                         |          },
                         |          {
                         |            "id": "2",
                         |            "name": "English 101",
                         |            "grade": 3.0
                         |          },
                         |          {
                         |            "id": "3",
                         |            "name": "Science 101",
                         |            "grade": 2.0
                         |          },
                         |          {
                         |            "id": "4",
                         |            "name": "Social Studies 101",
                         |            "grade": 2.5
                         |          },
                         |          {
                         |            "id": "5",
                         |            "name": "Health 101",
                         |            "grade": 3.5
                         |          },
                         |          {
                         |            "id": "6",
                         |            "name": "Chemistry 101",
                         |            "grade": 4.0
                         |          }
                         |        ]
                         |      },
                         |      {
                         |        "firstName": "Jane",
                         |        "lastName": "Smith",
                         |        "studentClasses": [
                         |          {
                         |            "id": "1",
                         |            "name": "Math 101",
                         |            "grade": 3.0
                         |          },
                         |          {
                         |            "id": "2",
                         |            "name": "English 101",
                         |            "grade": 2.0
                         |          },
                         |          {
                         |            "id": "3",
                         |            "name": "Science 101",
                         |            "grade": 2.5
                         |          },
                         |          {
                         |            "id": "5",
                         |            "name": "Health 101",
                         |            "grade": 3.5
                         |          },
                         |          {
                         |            "id": "6",
                         |            "name": "Chemistry 101",
                         |            "grade": 2.0
                         |          },
                         |          {
                         |            "id": "7",
                         |            "name": "English 201",
                         |            "grade": 3.5
                         |          }
                         |        ]
                         |      }
                         |    ]
                         |  }
                         |}""".stripMargin

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    val responseJson = response.body().string()
    printer.pretty(parse(responseJson).right.get) should be(printer.pretty(parse(expectedJson).right.get))
  }

  "GraphQL server" should "found students details when the first name is present in database" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/graphql"))
      .post(
        RequestBody.create(
          """
            |query FindStudentByFirstName {
            |  SearchStudentDetails(firstName: "Mike") {
            |    firstName
            |    lastName
            |    gpa
            |  }
            |}
            |""".stripMargin.getBytes()
        )
      )
      .build()

    val expectedJson = """{
                         |  "data": {
                         |    "SearchStudentDetails": [
                         |      {
                         |        "firstName": "Mike",
                         |        "lastName": "Williams",
                         |        "gpa": 2.6666666666666665
                         |      }
                         |    ]
                         |  }
                         |}""".stripMargin

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    val responseJson = response.body().string()
    response.code() shouldBe 200
    printer.pretty(parse(responseJson).right.get) should be(printer.pretty(parse(expectedJson).right.get))
  }

  "GraphQL server" should "found students details when the first name and last name are present in database" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/graphql"))
      .post(
        RequestBody.create(
          """
            |query FindStudentByFirstName {
            |  SearchStudentDetails(firstName: "Samantha", lastName: "Ware") {
            |    firstName
            |    lastName
            |    gpa
            |  }
            |}
            |""".stripMargin.getBytes()
        )
      )
      .build()

    val expectedJson = """{
                         |  "data": {
                         |    "SearchStudentDetails": [
                         |      {
                         |        "firstName": "Samantha",
                         |        "lastName": "Ware",
                         |        "gpa": 3.75
                         |      }
                         |    ]
                         |  }
                         |}""".stripMargin

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    val responseJson = response.body().string()
    response.code() shouldBe 200
    printer.pretty(parse(responseJson).right.get) should be(printer.pretty(parse(expectedJson).right.get))
  }

  "GraphQL server" should "return an empty list when the last name is not present in the database" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/graphql"))
      .post(
        RequestBody.create(
          """
            |query FindStudentByFirstName {
            |  SearchStudentDetails(lastName: "Gonzalez") {
            |    firstName
            |    lastName
            |    gpa
            |  }
            |}
            |""".stripMargin.getBytes()
        )
      )
      .build()

    val expectedJson = """{
                         |  "data": {
                         |    "SearchStudentDetails": [
                         |    ]
                         |  }
                         |}""".stripMargin

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    val responseJson = response.body().string()
    response.code() shouldBe 200
    printer.pretty(parse(responseJson).right.get) should be(printer.pretty(parse(expectedJson).right.get))
  }

  "GraphQL server" should "return an empty list when the first name is not present in the database" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/graphql"))
      .post(
        RequestBody.create(
          """
            |query FindStudentByFirstName {
            |  SearchStudentDetails(firstName: "Mariano") {
            |    firstName
            |    lastName
            |    gpa
            |  }
            |}
            |""".stripMargin.getBytes()
        )
      )
      .build()

    val expectedJson = """{
                         |  "data": {
                         |    "SearchStudentDetails": [
                         |    ]
                         |  }
                         |}""".stripMargin

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    val responseJson = response.body().string()
    response.code() shouldBe 200
    printer.pretty(parse(responseJson).right.get) should be(printer.pretty(parse(expectedJson).right.get))
  }

  "GraphQL server" should "return a 400 HTTP error when the graphql query is not valid" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/graphql"))
      .post(
        RequestBody.create(
          """
            |query ThisCanBeAnyValue {
            |  TheNamedQueryShouldExists(firstName: "Mariano") {
            |    firstName
            |    lastName
            |    gpa
            |  }
            |}
            |""".stripMargin.getBytes()
        )
      )
      .build()

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    response.code() shouldBe 400

  }

  "GraphQL server" should "return a 405 HTTP error when executing an invalid path" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/invalid-path"))
      .post(
        RequestBody.create(
          """
            |query FindStudentByFirstName {
            |  SearchStudentDetails(firstName: "Samantha", lastName: "Ware") {
            |    firstName
            |    lastName
            |    gpa
            |  }
            |}""".stripMargin.getBytes()
        )
      )
      .build()

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    response.code() shouldBe 500
  }
}
