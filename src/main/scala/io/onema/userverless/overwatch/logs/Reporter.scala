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

import com.typesafe.scalalogging.Logger
import io.onema.json.Extensions._
import io.onema.userverless.model.Log.{LogErrorMessage, LogMessage, Rename}
import io.onema.userverless.model.Metric
import io.onema.userverless.overwatch.logs.ParserLogic.{ParsedResults, Report, TimeoutError}
import io.onema.userverless.exception.ThrowableExtensions._
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.{PublishRequest, PublishResponse}

import scala.async.Async.async
import scala.compat.java8.FutureConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class Reporter(val snsClient: SnsAsyncClient, val errorTopic: Option[String], val notificationTopic: Option[String], val logTopic: Option[String], val metricTopic: Option[String]) {
  //--- Fields ---
  val log = Logger(classOf[Reporter])


  //--- Methods ---
  def publishResultsAsync(parsedResults: ParsedResults): Future[Seq[PublishResponse]] = async {
    log.debug(s"Submitting ${parsedResults.metrics.length} metrics")
    val met: Seq[Future[PublishResponse]] = metric(parsedResults.metrics).toSeq

    log.debug(s"Submitting ${parsedResults.errors.length} errors")
    val err: Seq[Future[PublishResponse]] = parsedResults.errors.flatMap(error)

    log.debug(s"Submitting ${parsedResults.metaspaceErrors.length} Metaspace errors")
    val metaspace: Seq[Future[PublishResponse]] = parsedResults.metaspaceErrors.flatMap(error)

    log.debug(s"Submitting ${parsedResults.timeoutError.length} timeout error")
    val to: Seq[Future[PublishResponse]] = parsedResults.timeoutError.flatMap(timeout)

    log.debug(s"Submitting ${parsedResults.report.length} reports")
    val rep: Seq[Future[PublishResponse]] = parsedResults.report.flatMap(report(_, parsedResults.functionName))

    log.debug(s"Submitting ${parsedResults.logMessages.length} logs")
    val logs: Seq[Future[PublishResponse]] = log(parsedResults.logMessages).toSeq

    val allFutures: Seq[Future[PublishResponse]] = met ++ err ++ metaspace ++ to ++ rep ++ logs
    allFutures
      .map(logErrors)
      .map(Await.result(_, 5.seconds))
  }


  def error(errorMessage: LogErrorMessage): Option[Future[PublishResponse]] = {
    if(errorMessage.reportException) {
      log.debug(s"""Publishing error error "${errorMessage.message}" """)
      errorTopic.map(x => snsClient.publish(snsRequest(x, errorMessage.asJson(Rename.errorMessage))).toScala)
    } else {
      log.info(s"""SKIPPING Error error "${errorMessage.message}" as it has been marked as ignored""")
      None
    }
  }

  def notification(message: String): Option[Future[PublishResponse]] = {
    notificationTopic.map(x => snsClient.publish(snsRequest(x, message)).toScala)
  }

  def log(message: Seq[LogMessage]): Option[Future[PublishResponse]] = {
    val msg: Seq[String] = message.map(_.asJson(Rename.logMessage))
    logTopic.map(x => snsClient.publish(snsRequest(x, msg.asJson)).toScala)
  }

  def metric(message: Seq[Metric]): Option[Future[PublishResponse]] = {
    metricTopic.map(x => snsClient.publish(snsRequest(x, message.asJson)).toScala)
  }

  def report(report: Report, functionName: String): Option[Future[PublishResponse]] = {
    val memoryPercent = (100/report.memorySize.value.toDouble) * report.maxMemoryUsed.value.toDouble
    val message = if(memoryPercent >= 100) {
      log.debug("Memory notification Out Of memory")
      Some(s"""The function "$functionName" has run out of memory""")
    } else if(memoryPercent > 60) {
      log.debug("Memory consumption over 60%")
      Some(s"""The function "$functionName" memory usage is at $memoryPercent%""")
    } else {
      None
    }
    message.flatMap(notification)
  }

  def timeout(error: TimeoutError): Option[Future[PublishResponse]] = {
    log.debug(s"Time out error ${error.message}")
    notification(s"""The fucntion "${error.functionName}" ${error.message} """)
  }

  private def snsRequest(topic: String, message: String): PublishRequest = PublishRequest.builder().topicArn(topic).message(message).build()

  private def logErrors(future: Future[PublishResponse]): Future[PublishResponse] = {
    future.onComplete {
      case Success(_) =>
      case Failure(ex) =>
        val msg = ex.structuredMessage(reportException = false)
        log.error(msg)
    }
    future
  }
}
