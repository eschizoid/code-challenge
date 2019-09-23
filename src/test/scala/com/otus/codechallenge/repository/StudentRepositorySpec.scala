package com.otus.codechallenge.repository

import com.otus.codechallenge.mongo.MongoConnection
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._

class StudentRepositorySpec extends FlatSpec with Matchers {
  val client = MongoConnection("mongodb", 27017, "mongo", "otus", "mongo")

  "Student repository" should "find a student by first name" in {
    // Prepare
    val studentRepository = StudentMongoRepository(client)

    // Exercise
    val students = Await.result(studentRepository.fetchStudentByName("Mike"), 5 seconds).head.students

    // Assert
    students should have size 1
  }

  "Student repository" should "find a student by last name" in {
    // Prepare
    val studentRepository = StudentMongoRepository(client)

    // Exercise
    val students = Await.result(studentRepository.fetchStudentByLastName("Jordan"), 5 seconds).head.students

    // Assert
    students should have size 1
  }

  "Student repository" should "find a student by first and last name" in {
    // Prepare
    val studentRepository = StudentMongoRepository(client)

    // Exercise
    val students       = Await.result(studentRepository.fetchStudentByNameAndLastName("Samantha", "Ware"), 5 seconds).head.students
    val student        = students.head
    val studentClasses = students.head.studentClasses

    // Assert
    students should have size 1
    studentClasses should have size 6
    student.first shouldBe "Samantha"
    student.last shouldBe "Ware"
    student.email shouldBe "sware@mailinator.com"
    student.gpa shouldBe 3.75
  }

  "Student repository" should "find some students by last name" in {
    // Prepare
    val studentRepository = StudentMongoRepository(client)

    // Exercise
    val students = Await.result(studentRepository.fetchStudentByLastName("Smith"), 5 seconds).head.students

    // Assert
    students should have size 2
  }
}
