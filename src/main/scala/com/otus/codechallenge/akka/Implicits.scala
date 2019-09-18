package com.otus.codechallenge.akka

import java.nio.charset.Charset

import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import sangria.ast.Document
import sangria.parser.QueryParser

import scala.collection.immutable.Seq

/**
  *  A couple of implicits that will help us tus doing the un-marshalling of the json payload.
  */
object Implicits {

  def unmarshallerContentTypes: Seq[ContentTypeRange] =
    mediaTypes.map(ContentTypeRange.apply)

  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(MediaType.applicationWithFixedCharset("graphql", HttpCharsets.`UTF-8`, "graphql"))

  implicit final val documentUnmarshaller: FromEntityUnmarshaller[Document] =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty => throw Unmarshaller.NoContentException
        case data =>
          import sangria.parser.DeliveryScheme.Throw
          QueryParser.parse(data.decodeString(Charset.forName("UTF-8")))
      }
}
