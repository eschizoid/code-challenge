package com.otus.codechallenge.mongo

import org.mongodb.scala.bson.ObjectId

/*
 * A collection of mongo entities use when retrieving student classes information
 */
case class Classes(_id: ObjectId, classes: Map[String, String])

object Classes {
  def apply(classes: Map[String, String]): Classes =
    new Classes(new ObjectId(), classes)
}
