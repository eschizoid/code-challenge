package com.otus.codechallenge.service

import com.otus.codechallenge.graphql.{StudentClasses, StudentDetails}
import com.otus.codechallenge.mongo
import com.otus.codechallenge.mongo.{Classes, Students}
import com.otus.codechallenge.repository.{ClassRepository, StudentRepository}
import com.otus.codechallenge.util.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait GraphqlFetcher {

  /**
    * Searches for students information using any combination of name and last name.
    *
    * @param firsName optional name to be used when searching for students
    * @param lastName optional last name to be used when searching for students
    * @return a list of students detailed information of type GraphQL
    */
  def searchAndTransformStudents(firsName: Option[String], lastName: Option[String]): Future[Seq[StudentDetails]]
}

/**
  * An opinionated class for enriching and transforming mongo entities.
  *
  * This class might be bit opinionated since one could possibly argue: why not returning the entities all the way down
  * to the graphql server? The answer is really simple, the mongo queries started getting little difficult to read and
  * doing that last aggregation for adding the class name to the student list was not an easy task.
  */
class GraphqlStudentsFetcher(studentRepository: StudentRepository, classRepository: ClassRepository) extends GraphqlFetcher with Logging {

  override def searchAndTransformStudents(firsName: Option[String], lastName: Option[String]): Future[Seq[StudentDetails]] = {
    logger.debug("Aggregating students and classes")
    (firsName, lastName) match {
      case (Some(firstName), Some(lastName)) =>
        for {
          classes  <- classRepository.fetchAll()
          students <- studentRepository.fetchStudentByNameAndLastName(firstName, lastName)
        } yield studentEntityToGraph(students, classes)
      case (Some(firstName), None) =>
        for {
          classes  <- classRepository.fetchAll()
          students <- studentRepository.fetchStudentByName(firstName)
        } yield studentEntityToGraph(students, classes)
      case (None, Some(lastName)) =>
        for {
          classes  <- classRepository.fetchAll()
          students <- studentRepository.fetchStudentByLastName(lastName)
        } yield studentEntityToGraph(students, classes)
      case (None, None) => Future { Seq.empty[StudentDetails] }
    }
  }

  private def studentEntityToGraph(studentEntities: Seq[Students], classEntities: Seq[Classes]): Seq[StudentDetails] = {
    studentEntities flatMap { studentEntity =>
      studentEntity.students map { student =>
        StudentDetails(student.first, student.last, student.email, student.gpa, classEntityToGraph(student.studentClasses, classEntities))
      }
    }
  }

  private def classEntityToGraph(studentClassesEntity: Seq[mongo.StudentClasses], classesEntity: Seq[Classes]): Seq[StudentClasses] = {
    val classesCatalogue = classesEntity.head.classes
    studentClassesEntity map { `class` =>
      StudentClasses(`class`.id.toString, classesCatalogue(`class`.id.toString), `class`.grade)
    }
  }
}

object GraphqlStudentsFetcher {
  def apply(studentRepository: StudentRepository, classRepository: ClassRepository): GraphqlStudentsFetcher = {
    new GraphqlStudentsFetcher(studentRepository: StudentRepository, classRepository: ClassRepository)
  }
}
