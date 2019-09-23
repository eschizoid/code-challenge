package com.otus.codechallenge.util

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  lazy val logger: Logger                                = LoggerFactory.getLogger(getClass)
  implicit def loggingToLogger(logging: Logging): Logger = logging.logger
}
