package com.otus.codechallenge.mongo

import org.mongodb.scala.bson.ObjectId

/*
 * A collection of mongo entities use when retrieving students information.
 */
case class StudentClasses(id: Int, grade: Double)

case class Student(first: String, last: String, email: String, studentClasses: Seq[StudentClasses]) {
  def gpa: Double = studentClasses.foldLeft(0.0)(_ + _.grade) / studentClasses.length
}

case class Students(_id: ObjectId, students: Seq[Student])

object Students {
  def apply(students: Seq[Student]): Students =
    new Students(new ObjectId(), students: Seq[Student])
}
