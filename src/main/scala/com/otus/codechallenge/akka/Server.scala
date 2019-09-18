package com.otus.codechallenge.akka

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.otus.codechallenge.akka.Implicits._
import com.otus.codechallenge.graphql.SchemaDefinition.schema
import com.otus.codechallenge.mongo.MongoConnection
import com.otus.codechallenge.repository.{ClassMongoRepository, StudentMongoRepository}
import com.otus.codechallenge.service.GraphqlStudentsFetcher
import com.otus.codechallenge.util.Logging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.optics.JsonPath._
import io.circe.parser._
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.circe._
import sangria.parser.DeliveryScheme.Try
import sangria.parser.{QueryParser, SyntaxError}
import sangria.slowlog.SlowLog

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * Main entry for starting the Actor System along with Akka routes. The application only has two endpoints, one for
  * serving GraphQL queries and the other one for serving the API documentation.
  *
  * ==GraphQL Endpoint==
  * {{{
  * POST /graphql -H "Content-Type: application/graphql" -q {...}
  * }}}
  *
  * ==API Docs Endpoint==
  * {{{
  * GET /api-docs -H "Content-Type: application/json"
  * }}}
  *
  * Note: Please see the spec of this class for more examples: [[com.otus.codechallenge.akka.ServerSpec]]
  */
object Server extends App with CorsSupport with Logging {
  logger.info(f"Starting HTTP server on port ${sys.env("API_PORT")}...")

  implicit val system: ActorSystem                 = ActorSystem("graphql-api")
  implicit val materializer: ActorMaterializer     = ActorMaterializer()
  implicit val executionContext: MessageDispatcher = system.dispatchers.lookup("akka.stream.default-blocking-io-dispatcher")

  val client = MongoConnection(
    sys.env("DATABASE_HOST"),
    sys.env("DATABASE_PORT").toInt,
    sys.env("DATABASE_USER"),
    sys.env("DATABASE_NAME"),
    sys.env("DATABASE_PASSWORD")
  )

  val fetcher = GraphqlStudentsFetcher(StudentMongoRepository(client), ClassMongoRepository(client))

  val route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing =>
      path("graphql") {
        post {
          parameters('query.?, 'operationName.?, 'variables.?) { (queryParam, operationNameParam, variablesParam) =>
            entity(as[Json]) { body =>
              val query     = queryParam orElse root.query.string.getOption(body)
              val operation = operationNameParam orElse root.operationName.string.getOption(body)
              val variables = variablesParam orElse root.variables.string.getOption(body)
              query map { QueryParser.parse(_) } match {
                case Some(Success(ast)) =>
                  variables map { parse } match {
                    case Some(Left(error)) => complete(BadRequest, formatError(error))
                    case Some(Right(json)) => executeGraphQL(ast, operation, json, tracing.isDefined)
                    case None =>
                      executeGraphQL(ast, operation, root.variables.json.getOption(body) getOrElse Json.obj(), tracing.isDefined)
                  }
                case Some(Failure(error)) => complete(BadRequest, formatError(error))
                case None                 => complete(BadRequest, formatError("No query to execute"))
              }
            } ~
              entity(as[Document]) { document =>
                variablesParam map { parse } match {
                  case Some(Left(error)) => complete(BadRequest, formatError(error))
                  case Some(Right(json)) => executeGraphQL(document, operationNameParam, json, tracing.isDefined)
                  case None              => executeGraphQL(document, operationNameParam, Json.obj(), tracing.isDefined)
                }
              }
          }
        }
      } ~
        path("api-docs") {
          get {
            complete(OK, executeApiDocs())
          }
        }
    } ~
      (get & pathEndOrSingleSlash) {
        redirect("/graphql", PermanentRedirect)
      }

  Http().bindAndHandle(corsHandler(route), "0.0.0.0", sys.env("API_PORT").toInt)

  logger.info("Akka routes initialized!")

  def executeGraphQL(query: Document, operationName: Option[String], variables: Json, tracing: Boolean) =
    complete(
      Executor
        .execute(
          schema,
          query,
          fetcher,
          variables = if (variables.isNull) Json.obj() else variables,
          operationName = operationName,
          middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil,
        )
        .map { OK -> _ }
        .recover {
          case error: QueryAnalysisError => BadRequest          -> error.resolveError
          case error: ErrorWithResolver  => InternalServerError -> error.resolveError
        })

  def executeApiDocs() =
    Await.result(Executor.execute(schema, sangria.introspection.introspectionQuery, fetcher), 10 seconds)

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError =>
      Json.obj(
        "errors" -> Json.arr(Json.obj(
          "message" -> Json.fromString(syntaxError.getMessage),
          "locations" -> Json.arr(Json.obj("line" -> Json.fromBigInt(syntaxError.originalError.position.line),
                                           "column" -> Json.fromBigInt(syntaxError.originalError.position.column)))
        )))
    case NonFatal(e) => Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(e.getMessage))))
    case e           => throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" -> Json.arr(Json.obj("message" -> Json.fromString(message))))
}
