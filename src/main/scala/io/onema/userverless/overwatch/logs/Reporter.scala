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

import com.amazonaws.services.sns.AmazonSNS
import com.typesafe.scalalogging.Logger
import io.onema.userverless.model.Log.{LogErrorMessage, LogMessage, Rename}
import io.onema.json.Extensions._
import io.onema.userverless.model.Metric
import io.onema.userverless.overwatch.logs.ParserLogic.Report

class Reporter(val snsClient: AmazonSNS, val errorTopic: Option[String], val notificationTopic: Option[String], val logTopic: Option[String], val metricTopic: Option[String]) {
  //--- Fields ---
  val logger = Logger(classOf[Reporter])

  //--- Methods ---
  def error(errorMessage: LogErrorMessage): Unit = {
    if(errorMessage.reportException) {
      logger.debug(s"""Publishing error error "${errorMessage.message}" """)
      errorTopic.foreach(snsClient.publish(_, errorMessage.asJson(Rename.errorMessage)))
    } else {
      logger.info(s"""SKIPPING Error error "${errorMessage.message}" as it has been marked as ignored""")
    }
  }

  def notification(message: String): Unit = {
    notificationTopic.foreach(snsClient.publish(_, message))
  }

  def log(message: Seq[LogMessage]): Unit = {
    val msg: Seq[String] = message.map(_.asJson(Rename.logMessage))
    logTopic.foreach(snsClient.publish(_, msg.asJson))
  }

  def metric(message: Seq[Metric]): Unit = {
    metricTopic.foreach(snsClient.publish(_, message.asJson))
  }

  def report(report: Report, functionName: String): Unit = {
    val memoryPercent = (100/report.memorySize.value.toDouble) * report.maxMemoryUsed.value.toDouble
    val message = if(memoryPercent >= 100) {
      Some(s"""The function "$functionName" has run out of memory""")
    } else if(memoryPercent > 80) {
      Some(s"""The function "$functionName" memory usage is at $memoryPercent%""")
    } else {
      None
    }
    message.foreach(notification)
  }
}
