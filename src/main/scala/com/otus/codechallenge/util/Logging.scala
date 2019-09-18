package com.otus.codechallenge.util

import org.slf4j.{Logger, LoggerFactory}

trait Logging {
  lazy val logger: Logger                                = LoggerFactory.getLogger(getClass)
  implicit def logging2Logger(anything: Logging): Logger = anything.logger
}
