package com.otus.codechallenge.graphql

import com.otus.codechallenge.service.GraphqlFetcher
import sangria.macros.derive._
import sangria.schema.{Field, ListType, ObjectType, _}

/**
  * Thanks to GraphQL specification, we only need one endpoint since we can control the return fields via a GraphQL
  * query.
  *
  * ==Sample Student Query==
  * {{{
  * query FindStudentByFirstName {
  *   SearchStudentDetails(firstName: "Mike") {
  *     firstName
  *     lastName
  *     gpa
  *   }
  * }
  * }}}
  *
  * ==Sample Student Details Query==
  * {{{
  * query FindStudentDetailsByFirstName {
  *   SearchStudentDetails(lastName: "Smith") {
  *     firstName
  *     lastName
  *     gpa
  *     emailAddress
  *     studentClasses {
  *       id
  *       name
  *       grade
  *     }
  *   }
  * }
  * }}}
  */
object SchemaDefinition {

  implicit val StudentClassesType: ObjectType[Unit, StudentClasses] = deriveObjectType[Unit, StudentClasses](
    ObjectTypeDescription("The product picture"),
    DocumentField("id", "Class id"),
    DocumentField("name", "Class name"),
    DocumentField("grade", "Class grade"),
  )

  implicit val StudentDetailsType: ObjectType[Unit, StudentDetails] = deriveObjectType[Unit, StudentDetails](
    ObjectTypeDescription("Student Detail Information"),
    DocumentField("firstName", "Student first name"),
    DocumentField("lastName", "Student last name"),
    DocumentField("gpa", "Student GPA"),
    DocumentField("emailAddress", "Student email"),
    DocumentField("studentClasses", "Student classes")
  )

  val FirstName = Argument("firstName", OptionInputType(StringType))
  val LastName  = Argument("lastName", OptionInputType(StringType))

  val QueryType = ObjectType(
    "Query",
    fields[GraphqlFetcher, Unit](
      Field(
        "SearchStudentDetails",
        ListType(StudentDetailsType),
        description = Some("Returns a list of students with detailed information"),
        arguments = List(FirstName, LastName),
        resolve = c => c.ctx.searchAndTransformStudents(c.arg(FirstName), c.arg(LastName))
      )
    )
  )

  val schema = Schema(QueryType)
}
