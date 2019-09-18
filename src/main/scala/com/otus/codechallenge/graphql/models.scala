package com.otus.codechallenge.graphql

/*
 * Case classes used to build the graphql type system.
 */
case class StudentClasses(id: String, name: String, grade: Double)

case class StudentDetails(firstName: String, lastName: String, emailAddress: String, gpa: Double, studentClasses: Seq[StudentClasses])
