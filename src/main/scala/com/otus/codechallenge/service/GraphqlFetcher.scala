package com.otus.codechallenge.service

import com.otus.codechallenge.graphql.{StudentClasses, StudentDetails}
import com.otus.codechallenge.mongo
import com.otus.codechallenge.mongo.Students
import com.otus.codechallenge.repository.{ClassRepository, StudentRepository}
import com.otus.codechallenge.util.Logging

import scala.concurrent.Await.result
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

trait GraphqlFetcher {

  /**
    * Searches for students information using any combination of name and last name.
    *
    * @param firsName optional name to be used when searching for students
    * @param lastName optional last name to be used when searching for students
    * @return a list of students detailed information of type GraphQL
    */
  def searchAndTransformStudents(firsName: Option[String], lastName: Option[String]): Seq[StudentDetails]
}

/**
  * An opinionated class for enriching and transforming mongo entities.
  *
  * This class might be bit opinionated since one could possibly argue: why not returning the entities all the way down
  * to the graphql server? The answer is really simple, the mongo queries started getting little bit difficult to read
  * and doing that last aggregation for adding the class name to the student list was not an easy task.
  *
  */
class GraphqlStudentsFetcher(studentRepository: StudentRepository, classRepository: ClassRepository) extends GraphqlFetcher with Logging {

  override def searchAndTransformStudents(firsName: Option[String], lastName: Option[String]): Seq[StudentDetails] = {
    logger.debug("Aggregating students and classes")
    (firsName, lastName) match {
      case (Some(firstName), Some(lastName)) =>
        Try(result(studentRepository.fetchStudentByNameAndLastName(firstName, lastName), 10 seconds)) match {
          case Success(entities) => studentDetailedInformationEntityToGraph(entities)
          case Failure(e) =>
            logger.error(f"Unable to find students information using first and last name [$firstName $lastName]: $e")
            Seq.empty[StudentDetails]
        }
      case (Some(firstName), None) =>
        Try(result(studentRepository.fetchStudentByName(firstName), 10 seconds)) match {
          case Success(entities) => studentDetailedInformationEntityToGraph(entities)
          case Failure(e) =>
            logger.error(f"Unable to find students information using firstr name [$firstName]: $e")
            Seq.empty[StudentDetails]
        }
      case (None, Some(lastName)) =>
        Try(result(studentRepository.fetchStudentByLastName(lastName), 10 seconds)) match {
          case Success(entities) => studentDetailedInformationEntityToGraph(entities)
          case Failure(e) =>
            logger.error(f"Unable to find students information using last name [$lastName]: $e")
            Seq.empty[StudentDetails]
        }
      case (None, None) => Seq.empty[StudentDetails]
    }
  }

  private def studentDetailedInformationEntityToGraph(entities: Seq[Students]): Seq[StudentDetails] = {
    entities flatMap { entity =>
      entity.students map { studentEntity =>
        StudentDetails(studentEntity.first,
                       studentEntity.last,
                       studentEntity.email,
                       studentEntity.gpa,
                       classEntityToGraph(studentEntity.studentClasses))
      }
    }
  }

  private def classEntityToGraph(studentClassesEntity: Seq[mongo.StudentClasses]): Seq[StudentClasses] = {
    // TODO we should consider caching this entity if we dont figure out how to cache at the GraphQL Server
    Try(result(classRepository.fetchAll(), 10 seconds)) match {
      case Success(entities) =>
        val classesCatalogue = entities.head.classes
        studentClassesEntity.map(`class` => StudentClasses(`class`.id.toString, classesCatalogue(`class`.id.toString), `class`.grade))
      case Failure(e) =>
        logger.error(f"Unable to retrieve class catalogue: $e")
        Seq.empty[StudentClasses]
    }
  }
}

object GraphqlStudentsFetcher {
  def apply(studentRepository: StudentRepository, classRepository: ClassRepository): GraphqlStudentsFetcher = {
    new GraphqlStudentsFetcher(studentRepository: StudentRepository, classRepository: ClassRepository)
  }
}
