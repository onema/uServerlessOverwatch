/**
  * This file is part of the ONEMA userverless-overwatch Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2019, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.overwatch.logs

import com.amazonaws.services.sns.AmazonSNSAsync
import com.amazonaws.services.sns.model.PublishResult
import com.typesafe.scalalogging.Logger
import io.onema.json.Extensions._
import io.onema.userverless.model.Log.{LogErrorMessage, LogMessage, Rename}
import io.onema.userverless.model.Metric
import io.onema.userverless.overwatch.logs.ParserLogic.{ParsedResults, Report, TimeoutError}
import io.onema.userverless.exception.ThrowableExtensions._
import io.onema.userverless.overwatch.logs.Reporter._

import scala.async.Async.async
import java.util.concurrent.{Future => JFuture}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class Reporter(val snsClient: AmazonSNSAsync, val errorTopic: Option[String], val notificationTopic: Option[String], val logTopic: Option[String], val metricTopic: Option[String]) {

  //--- Fields ---
  val log = Logger(classOf[Reporter])

  //--- Methods ---
  def publishResultsAsync(parsedResults: ParsedResults): Future[Seq[PublishResult]] = async {
    log.debug(s"Submitting ${parsedResults.metrics.length} metrics")
    val met: Seq[Future[PublishResult]] = metric(parsedResults.metrics).toSeq

    log.debug(s"Submitting ${parsedResults.errors.length} errors")
    val err: Seq[Future[PublishResult]] = parsedResults.errors.flatMap(error)

    log.debug(s"Submitting ${parsedResults.metaspaceErrors.length} Metaspace errors")
    val metaspace: Seq[Future[PublishResult]] = parsedResults.metaspaceErrors.flatMap(error)

    log.debug(s"Submitting ${parsedResults.timeoutError.length} timeout error")
    val to: Seq[Future[PublishResult]] = parsedResults.timeoutError.flatMap(timeout)

    log.debug(s"Submitting ${parsedResults.report.length} reports")
    val rep: Seq[Future[PublishResult]] = parsedResults.report.flatMap(report(_, parsedResults.functionName))

    log.debug(s"Submitting ${parsedResults.logMessages.length} logs")
    val logs: Seq[Future[PublishResult]] = log(parsedResults.logMessages).toSeq

    val allFutures: Seq[Future[PublishResult]] = met ++ err ++ metaspace ++ to ++ rep ++ logs
    allFutures
      .map(logErrors)
      .map(Await.result(_, 5.seconds))
  }


  def error(errorMessage: LogErrorMessage): Option[Future[PublishResult]] = {
    if(errorMessage.reportException) {
      log.debug(s"""Publishing error error "${errorMessage.message}" """)
      errorTopic.map(snsClient.publishAsync(_, errorMessage.asJson(Rename.errorMessage)).asScala)
    } else {
      log.info(s"""SKIPPING Error error "${errorMessage.message}" as it has been marked as ignored""")
      None
    }
  }

  def notification(message: String): Option[Future[PublishResult]] = {
    notificationTopic.map(snsClient.publishAsync(_, message).asScala)
  }

  def log(message: Seq[LogMessage]): Option[Future[PublishResult]] = {
    val msg: Seq[String] = message.map(_.asJson(Rename.logMessage))
    logTopic.map(snsClient.publishAsync(_, msg.asJson).asScala)
  }

  def metric(message: Seq[Metric]): Option[Future[PublishResult]] = {
    metricTopic.map(snsClient.publishAsync(_, message.asJson).asScala)
  }

  def report(report: Report, functionName: String): Option[Future[PublishResult]] = {
    val memoryPercent = (100/report.memorySize.value.toDouble) * report.maxMemoryUsed.value.toDouble
    val message = if(memoryPercent >= 100) {
      log.debug("Memory notification Out Of memory")
      Some(s"""The function "$functionName" has run out of memory""")
    } else if(memoryPercent > 60) {
      log.debug("Memory consumption over 60%")
      Some(Notification(functionName, s"""The function "$functionName" memory usage is at $memoryPercent%""").asJson)
    } else {
      None
    }
    message.flatMap(notification)
  }

  def timeout(error: TimeoutError): Option[Future[PublishResult]] = {
    log.debug(s"Time out error ${error.message}")
    notification(Notification(error.functionName, s"""The fucntion "${error.functionName}" ${error.message} """).asJson)
  }

  private def logErrors(future: Future[PublishResult]): Future[PublishResult] = {
    future.onComplete {
      case Success(_) =>
      case Failure(ex) =>
        val msg = ex.structuredMessage(reportException = false)
        log.error(msg)
    }
    future
  }
}

object Reporter {
  implicit class JFutureExtensions[PublishResult](jFuture: JFuture[PublishResult]) {
    def asScala: Future[PublishResult] = {
      Future {
        jFuture.get()
      }
    }
  }

  case class Notification(functionName: String, message: String)
}
