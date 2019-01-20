/**
  * This file is part of the ONEMA userverless-overwatch Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2018, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.overwatch.logs

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Base64
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.typesafe.scalalogging.Logger
import io.onema.json.Extensions._
import io.onema.userverless.model.Log.{LogErrorMessage, LogMessage, Rename}
import io.onema.userverless.model.Metric

import scala.util.{Failure, Success, Try}

object ParserLogic {

  //--- Fields ---
  val errorPrefix = "*** ERROR :"
  val metricPrefix = "*** METRIC :"
  val reportPrefix = "REPORT"
  val logRegex = "[\\*]+ [A-Z\\s]+:"
  val reportRegex = "\\t+| ms[ ]*| MB[ ]*"
  val log = Logger("parser-logic")

  //--- Methods ---
  def parse(encodedMessage: String): ParsedResults = {
    val decodedLogs = decodeEvent(encodedMessage)
    implicit val messages: Seq[String] = decodedLogs.logEvents.map(_.message)
    val functionName = decodedLogs.logGroup.getOrElse("NA").split("/").last
    log.debug(s"Decoding errors $errors")
    log.debug(s"Decoding metrics $metrics")
    log.debug(s"Decoding all others $report")
    log.debug(s"Decoding all others $logs")

    ParsedResults(functionName, errors, metrics, logs, report)
  }

  final def decodeEvent(base64Event: String): Logs = {
    val gzipBytes = Base64.getDecoder.decode(base64Event)
    val gzip = new GZIPInputStream(new ByteArrayInputStream(gzipBytes))
    val str = scala.io.Source.fromInputStream(gzip).mkString
    str.jsonDecode[Logs]
  }

  final def encodeEvent(str: String): String = {
    val os = new ByteArrayOutputStream(str.getBytes.length)
    val gz = new GZIPOutputStream(os)
    gz.write(str.getBytes())
    gz.close()
    val compressed = os.toByteArray
    os.close()
    Base64.getEncoder.encodeToString(compressed)
  }

  def errors()(implicit messages: Seq[String]): Seq[LogErrorMessage] = {
    messages.filter(_.startsWith(errorPrefix))
      .map(x => {
        x.stripPrefix(errorPrefix)
          .stripSuffix("\n")
          .jsonDecode[LogErrorMessage](Rename.errorMessage)
      })
  }

  def metrics()(implicit messages: Seq[String]): Seq[Metric] = {
    val metricLogs = messages.filter(_.startsWith(metricPrefix))
      .map(x => {
        x.stripPrefix(metricPrefix)
          .stripSuffix("\n")
          .jsonDecode[LogMessage](Rename.logMessage)
      })
      metricLogs
        .groupBy(_.appName)
        .flatMap { case (func, v) => v.map(m => {
        Try(parseMetric(m.message, func)) match {
          case Success(metric) => Some(metric)
          case Failure(ex) =>
            // Log the error, but don't fail
            log.error(ex.getMessage)
            None
        }
      }).filter(_.isDefined).flatten
      }.toSeq
  }

  def logs()(implicit messages: Seq[String]): Seq[LogMessage] = {
    messages.filter(x => !x.startsWith(errorPrefix) && !x.startsWith(metricPrefix) && !x.startsWith(reportPrefix))
      .map(x => {
        x.replaceFirst(logRegex, "")
          .stripSuffix("\n")
          .jsonDecode[LogMessage](Rename.logMessage)
      })
  }

  def report()(implicit  messages: Seq[String]): Seq[Report] = {
    messages.filter(x => x.startsWith(reportPrefix))
      .map(x => {
        // Remove trailing tabs
        val reportEntries = x.stripSuffix("\t\n")
          // Remove the Report prefix
          .stripPrefix(reportPrefix)
          // split on tabs, ms, and MB
          .split(reportRegex)
          // remove any empty values
          .filter(_.nonEmpty)
          // convert strings to reports
          .map(toReportEntry)
        val requestId = reportEntries.headOption.getOrElse(throw new Exception(s"""Invalid report "$x""""))
        Report(
          requestId.value,
          duration = reportEntries(1),
          billedDuration = reportEntries(2),
          memorySize = reportEntries(3),
          maxMemoryUsed = reportEntries(4)
        )
      })
  }

    /**
      * Split strings of the form "key: value"
      * @return Report object
      */
  def toReportEntry(reportStr: String): ReportEntry = {
    reportStr
      .split(": ")
      .grouped(2)
      .map { case Array(k, v) =>
        val unit = k.trim match {
          case ReportTypes.DURATION() => "ms"
          case ReportTypes.BILLED_DURATION() => "ms"
          case ReportTypes.MEMORY_SIZE() => "MB"
          case ReportTypes.MEMORY_USED() => "MB"
          case _ => "NA"
        }
        ReportEntry(k.toLowerCase, v, unit)
      }.toSeq
      .headOption.getOrElse(throw new Exception(s"""Invalid report string "$reportStr". Must be in the form "key: value""""))
  }

  def parseMetric(metric: String, appName: String): Metric = {
    log.info(s"PARSING: $metric")
    val metricSection :: tags :: _= metric.split("#").toList
    val name :: metrics :: _ = metricSection.split(":").toList
    val value :: metricType :: sampleRate :: _ = metrics.split("\\|").toList
    val tagMap = tags
      .split(":|,") // Split by ':' or ','
      .grouped(2) // Make groups of two
      .map { case Array(k, v) => k -> v } // make key value pairs and transform it to a map
      .toMap
    Metric(name, value.toDouble, metricType, sampleRate, tagMap, appName)
  }

  case class LogEvents(id: Option[String], timestamp: Option[Double], message: String)

  case class Logs(
    messageType: Option[String],
    owner: Option[String],
    logGroup: Option[String],
    logStream: Option[String],
    subscriptionFilters: Option[List[String]],
    logEvents: List[LogEvents]
  )

  case class Report(requestId: String, duration: ReportEntry, billedDuration: ReportEntry, memorySize: ReportEntry, maxMemoryUsed: ReportEntry)

  case class ReportEntry(name: String, value: String, reportUnit: String)

  case class ParsedResults(functionName: String, errors: Seq[LogErrorMessage], metrics: Seq[Metric], logMessages: Seq[LogMessage], report: Seq[Report])
}

object ReportTypes extends Enumeration {
  val DURATION = ReportValue("Duration")
  val BILLED_DURATION = ReportValue("Billed Duration")
  val MEMORY_SIZE = ReportValue("Memory Size")
  val MEMORY_USED = ReportValue("Max Memory Used")

  def ReportValue(name: String): Value with Matching = new Val(nextId, name) with Matching

  // enables matching against all Role.Values
  def unapply(s: String): Option[Value] = values.find(s == _.toString)

  trait Matching {
    // enables matching against a particular Role.Value
    def unapply(s: String): Boolean = s == toString
  }
}