package com.otus.codechallenge.akka

import io.circe.parser._
import io.circe.{Printer, _}
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

  "GraphQL server" should "find students details when the last name is present in database" in {

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

  "GraphQL server" should "find students details when the first name is present in database" in {

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

  "GraphQL server" should "find students details when the first name and last name are present in database" in {

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

  "GraphQL server" should "return tracing information when X-Apollo-Tracing HTTP header is provided" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .header("X-Apollo-Tracing", "true")
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

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    for {
      j          <- parser.parse(response.body().string()).toOption
      jObject    <- j.asObject
      extensions <- jObject("extensions")
    } yield {
      response.code() shouldBe 200
      extensions should be(a[Json])
    }
  }

  "GraphQL server" should "return API Docs" in {

    // Prepare
    val request = new Request.Builder()
      .header("Content-Type", "application/graphql")
      .header("Accept", "application/json")
      .header("Accept-Charset", "utf-8")
      .url(baseApiUrl.resolve("/api-docs"))
      .get()
      .build()

    val expectedJson =
      """{"data":{"__schema":{"queryType":{"name":"Query"},"mutationType":null,"subscriptionType":null,"types":[{"kind":"OBJECT","name":"Query","description":null,"fields":[{"name":"SearchStudentDetails","description":"Returns a list of students with detailed information","args":[{"name":"firstName","description":null,"type":{"kind":"SCALAR","name":"String","ofType":null},"defaultValue":null},{"name":"lastName","description":null,"type":{"kind":"SCALAR","name":"String","ofType":null},"defaultValue":null}],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"StudentDetails","ofType":null}}}},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"StudentClasses","description":"The product picture","fields":[{"name":"id","description":"Class id","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"name","description":"Class name","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"grade","description":"Class grade","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Float","ofType":null}},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"StudentDetails","description":"Student Detail Information","fields":[{"name":"firstName","description":"Student first name","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"lastName","description":"Student last name","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"emailAddress","description":"Student email","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"gpa","description":"Student GPA","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Float","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"studentClasses","description":"Student classes","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"StudentClasses","ofType":null}}}},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"__Directive","description":"A Directive provides a way to describe alternate runtime execution and type validation behavior in a GraphQL document.\n\nIn some cases, you need to provide options to alter GraphQL’s execution behavior in ways field arguments will not suffice, such as conditionally including or skipping a field. Directives provide this by describing additional information to the executor.","fields":[{"name":"name","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"description","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"locations","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"ENUM","name":"__DirectiveLocation","ofType":null}}}},"isDeprecated":false,"deprecationReason":null},{"name":"args","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__InputValue","ofType":null}}}},"isDeprecated":false,"deprecationReason":null},{"name":"onOperation","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"isDeprecated":true,"deprecationReason":"Use `locations`."},{"name":"onFragment","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"isDeprecated":true,"deprecationReason":"Use `locations`."},{"name":"onField","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"isDeprecated":true,"deprecationReason":"Use `locations`."}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"ENUM","name":"__DirectiveLocation","description":"A Directive can be adjacent to many parts of the GraphQL language, a __DirectiveLocation describes one such possible adjacencies.","fields":null,"inputFields":null,"interfaces":null,"enumValues":[{"name":"QUERY","description":"Location adjacent to a query operation.","isDeprecated":false,"deprecationReason":null},{"name":"MUTATION","description":"Location adjacent to a mutation operation.","isDeprecated":false,"deprecationReason":null},{"name":"SUBSCRIPTION","description":"Location adjacent to a subscription operation.","isDeprecated":false,"deprecationReason":null},{"name":"FIELD","description":"Location adjacent to a field.","isDeprecated":false,"deprecationReason":null},{"name":"FRAGMENT_DEFINITION","description":"Location adjacent to a fragment definition.","isDeprecated":false,"deprecationReason":null},{"name":"FRAGMENT_SPREAD","description":"Location adjacent to a fragment spread.","isDeprecated":false,"deprecationReason":null},{"name":"INLINE_FRAGMENT","description":"Location adjacent to an inline fragment.","isDeprecated":false,"deprecationReason":null},{"name":"SCHEMA","description":"Location adjacent to a schema definition.","isDeprecated":false,"deprecationReason":null},{"name":"SCALAR","description":"Location adjacent to a scalar definition.","isDeprecated":false,"deprecationReason":null},{"name":"OBJECT","description":"Location adjacent to an object type definition.","isDeprecated":false,"deprecationReason":null},{"name":"FIELD_DEFINITION","description":"Location adjacent to a field definition.","isDeprecated":false,"deprecationReason":null},{"name":"ARGUMENT_DEFINITION","description":"Location adjacent to an argument definition.","isDeprecated":false,"deprecationReason":null},{"name":"INTERFACE","description":"Location adjacent to an interface definition.","isDeprecated":false,"deprecationReason":null},{"name":"UNION","description":"Location adjacent to a union definition.","isDeprecated":false,"deprecationReason":null},{"name":"ENUM","description":"Location adjacent to an enum definition.","isDeprecated":false,"deprecationReason":null},{"name":"ENUM_VALUE","description":"Location adjacent to an enum value definition.","isDeprecated":false,"deprecationReason":null},{"name":"INPUT_OBJECT","description":"INPUT_OBJECT","isDeprecated":false,"deprecationReason":null},{"name":"INPUT_FIELD_DEFINITION","description":"Location adjacent to an input object field definition.","isDeprecated":false,"deprecationReason":null}],"possibleTypes":null},{"kind":"OBJECT","name":"__EnumValue","description":"One possible value for a given Enum. Enum values are unique values, not a placeholder for a string or numeric value. However an Enum value is returned in a JSON response as a string.","fields":[{"name":"name","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"description","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"isDeprecated","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"deprecationReason","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"__Field","description":"Object and Interface types are described by a list of Fields, each of which has a name, potentially a list of arguments, and a return type.","fields":[{"name":"name","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"description","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"args","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__InputValue","ofType":null}}}},"isDeprecated":false,"deprecationReason":null},{"name":"type","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Type","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"isDeprecated","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"deprecationReason","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"__InputValue","description":"Arguments provided to Fields or Directives and the input fields of an InputObject are represented as Input Values which describe their type and optionally a default value.","fields":[{"name":"name","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"String","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"description","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"type","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Type","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"defaultValue","description":"A GraphQL-formatted string representing the default value for this input value.","args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"__Schema","description":"A GraphQL Schema defines the capabilities of a GraphQL server. It exposes all available types and directives on the server, as well as the entry points for query, mutation, and subscription operations.","fields":[{"name":"types","description":"A list of all types supported by this server.","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Type","ofType":null}}}},"isDeprecated":false,"deprecationReason":null},{"name":"queryType","description":"The type that query operations will be rooted at.","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Type","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"mutationType","description":"If this server supports mutation, the type that mutation operations will be rooted at.","args":[],"type":{"kind":"OBJECT","name":"__Type","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"subscriptionType","description":"If this server support subscription, the type that subscription operations will be rooted at.","args":[],"type":{"kind":"OBJECT","name":"__Type","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"directives","description":"A list of all directives supported by this server.","args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Directive","ofType":null}}}},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"OBJECT","name":"__Type","description":"The fundamental unit of any GraphQL Schema is the type. There are many kinds of types in GraphQL as represented by the `__TypeKind` enum.\n\nDepending on the kind of a type, certain fields describe information about that type. Scalar types provide no information beyond a name and description, while Enum types provide their values. Object and Interface types provide the fields they describe. Abstract types, Union and Interface, provide the Object types possible at runtime. List and NonNull types compose other types.","fields":[{"name":"kind","description":null,"args":[],"type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"ENUM","name":"__TypeKind","ofType":null}},"isDeprecated":false,"deprecationReason":null},{"name":"name","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"description","description":null,"args":[],"type":{"kind":"SCALAR","name":"String","ofType":null},"isDeprecated":false,"deprecationReason":null},{"name":"fields","description":null,"args":[{"name":"includeDeprecated","description":null,"type":{"kind":"SCALAR","name":"Boolean","ofType":null},"defaultValue":"false"}],"type":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Field","ofType":null}}},"isDeprecated":false,"deprecationReason":null},{"name":"interfaces","description":null,"args":[],"type":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Type","ofType":null}}},"isDeprecated":false,"deprecationReason":null},{"name":"possibleTypes","description":null,"args":[],"type":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__Type","ofType":null}}},"isDeprecated":false,"deprecationReason":null},{"name":"enumValues","description":null,"args":[{"name":"includeDeprecated","description":null,"type":{"kind":"SCALAR","name":"Boolean","ofType":null},"defaultValue":"false"}],"type":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__EnumValue","ofType":null}}},"isDeprecated":false,"deprecationReason":null},{"name":"inputFields","description":null,"args":[],"type":{"kind":"LIST","name":null,"ofType":{"kind":"NON_NULL","name":null,"ofType":{"kind":"OBJECT","name":"__InputValue","ofType":null}}},"isDeprecated":false,"deprecationReason":null},{"name":"ofType","description":null,"args":[],"type":{"kind":"OBJECT","name":"__Type","ofType":null},"isDeprecated":false,"deprecationReason":null}],"inputFields":null,"interfaces":[],"enumValues":null,"possibleTypes":null},{"kind":"ENUM","name":"__TypeKind","description":"An enum describing what kind of type a given `__Type` is.","fields":null,"inputFields":null,"interfaces":null,"enumValues":[{"name":"SCALAR","description":"Indicates this type is a scalar.","isDeprecated":false,"deprecationReason":null},{"name":"OBJECT","description":"Indicates this type is an object. `fields` and `interfaces` are valid fields.","isDeprecated":false,"deprecationReason":null},{"name":"INTERFACE","description":"Indicates this type is an interface. `fields` and `possibleTypes` are valid fields.","isDeprecated":false,"deprecationReason":null},{"name":"UNION","description":"Indicates this type is a union. `possibleTypes` is a valid field.","isDeprecated":false,"deprecationReason":null},{"name":"ENUM","description":"Indicates this type is an enum. `enumValues` is a valid field.","isDeprecated":false,"deprecationReason":null},{"name":"INPUT_OBJECT","description":"Indicates this type is an input object. `inputFields` is a valid field.","isDeprecated":false,"deprecationReason":null},{"name":"LIST","description":"Indicates this type is a list. `ofType` is a valid field.","isDeprecated":false,"deprecationReason":null},{"name":"NON_NULL","description":"Indicates this type is a non-null. `ofType` is a valid field.","isDeprecated":false,"deprecationReason":null}],"possibleTypes":null},{"kind":"SCALAR","name":"Boolean","description":"The `Boolean` scalar type represents `true` or `false`.","fields":null,"inputFields":null,"interfaces":null,"enumValues":null,"possibleTypes":null},{"kind":"SCALAR","name":"Float","description":"The `Float` scalar type represents signed double-precision fractional values as specified by [IEEE 754](http://en.wikipedia.org/wiki/IEEE_floating_point).","fields":null,"inputFields":null,"interfaces":null,"enumValues":null,"possibleTypes":null},{"kind":"SCALAR","name":"String","description":"The `String` scalar type represents textual data, represented as UTF-8 character sequences. The String type is most often used by GraphQL to represent free-form human-readable text.","fields":null,"inputFields":null,"interfaces":null,"enumValues":null,"possibleTypes":null}],"directives":[{"name":"include","description":"Directs the executor to include this field or fragment only when the `if` argument is true.","locations":["FIELD","FRAGMENT_SPREAD","INLINE_FRAGMENT"],"args":[{"name":"if","description":"Included when true.","type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"defaultValue":null}]},{"name":"skip","description":"Directs the executor to skip this field or fragment when the `if` argument is true.","locations":["FIELD","FRAGMENT_SPREAD","INLINE_FRAGMENT"],"args":[{"name":"if","description":"Included when true.","type":{"kind":"NON_NULL","name":null,"ofType":{"kind":"SCALAR","name":"Boolean","ofType":null}},"defaultValue":null}]},{"name":"deprecated","description":"Marks an element of a GraphQL schema as no longer supported.","locations":["ENUM_VALUE","FIELD_DEFINITION"],"args":[{"name":"reason","description":"Explains why this element was deprecated, usually also including a suggestion for how to access supported similar data. Formatted in [Markdown](https://daringfireball.net/projects/markdown/).","type":{"kind":"SCALAR","name":"String","ofType":null},"defaultValue":"\"No longer supported\""}]}]}}}"""

    // Execute
    val response = client.newCall(request).execute()

    // Assert
    val responseJson = response.body().string()
    response.code() shouldBe 200
    responseJson should be(expectedJson)
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
    response.code() shouldBe 405
  }
}
