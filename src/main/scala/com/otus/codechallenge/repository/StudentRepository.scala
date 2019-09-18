package com.otus.codechallenge.repository

import com.otus.codechallenge.mongo.Students
import com.otus.codechallenge.util.Logging
import org.mongodb.scala.bson.collection.Document
import org.mongodb.scala.{MongoCollection, MongoDatabase}

import scala.concurrent.Future
import scala.language.reflectiveCalls

trait StudentRepository {

  /**
    * Fetches students by name in async fashion way
    * @param name the student name
    * @return a list of students
    *
    * ==Sample mongo query==
    * {{{
    * {
    *   $project: {
    *     students: {
    *       $filter: {
    *         input: "$students",
    *         as: "student",
    *         cond: { $eq: [ "$$student.first", "Mike" ] }
    *       }
    *     }
    *   }
    * }
    * }}}
    */
  def fetchStudentByName(name: String): Future[Seq[Students]]

  /**
    * Fetches students by last in async fashion way
    * @param last the student last name
    * @return a list of students
    *
    * ==Sample mongo query==
    * {{{
    *{
    *  $project: {
    *    students: {
    *      $filter: {
    *        input: "$students",
    *        as: "student",
    *        cond: { $eq: [ "$$student.last", "Jordan" ] }
    *      }
    *    }
    *  }
    *}
    * }}}
    */
  def fetchStudentByLastName(last: String): Future[Seq[Students]]

  /**
    * Fetches students by last in async fashion way
    * @param name the student name
    * @param last the student last name
    * @return a list of students
    *
    * ==Sample mongo query==
    * {{{
    *{
    *  $project: {
    *    students: {
    *      $filter: {
    *        input: "$students",
    *        as: "student",
    *        cond: { $and: [
    *            { $eq: [ "$$student.first", "Mike" ] },
    *            { $eq: [ "$$student.last", "Jordan" ] }
    *        ] }
    *      }
    *    }
    *  }
    *}
    * }}}
    */
  def fetchStudentByNameAndLastName(name: String, last: String): Future[Seq[Students]]
}

/**
  * Repository class for returning Student entities
  * @param client the mongo client
  */
class StudentMongoRepository(client: MongoDatabase) extends StudentRepository with Logging {

  val collection: MongoCollection[Students] = client.getCollection("students")

  override def fetchStudentByName(name: String): Future[Seq[Students]] = {
    logger.debug(f"Fetching students by name: $name")
    val doc = Document(
      "$project" ->
        Document(
          "students" ->
            Document(
              "$filter" -> Document("input" -> "$students", "as" -> "student", "cond" -> Document("$eq" -> Seq("$$student.first", name))))))
    collection.aggregate(Seq(doc)).toFuture()
  }

  override def fetchStudentByLastName(last: String): Future[Seq[Students]] = {
    logger.debug(f"Fetching students by last name: $last")
    val doc = Document(
      "$project" ->
        Document(
          "students" ->
            Document(
              "$filter" -> Document("input" -> "$students", "as" -> "student", "cond" -> Document("$eq" -> Seq("$$student.last", last))))))
    collection.aggregate(Seq(doc)).toFuture()
  }

  override def fetchStudentByNameAndLastName(name: String, last: String): Future[Seq[Students]] = {
    logger.debug(f"Fetching students by name: $name and last name: $last")
    val doc = Document(
      "$project" ->
        Document(
          "students" ->
            Document("$filter" -> Document(
              "input" -> "$students",
              "as"    -> "student",
              "cond" -> Document(
                "$and" -> Seq(Document("$eq" -> Seq("$$student.first", name)), Document("$eq" -> Seq("$$student.last", last))))
            ))))
    collection.aggregate(Seq(doc)).toFuture()
  }
}

object StudentMongoRepository {
  def apply(client: MongoDatabase): StudentMongoRepository = {
    new StudentMongoRepository(client: MongoDatabase)
  }
}
