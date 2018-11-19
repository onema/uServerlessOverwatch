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

object ParserLogic {

  //--- Fields ---
  val errorPrefix = "*** ERROR :"
  val metricPrefix = "*** METRIC :"
  val logRegex = "[\\*]+ [A-Z\\s]+:"
  val log = Logger("parser-logic")

  //--- Methods ---
  def parse(encodedMessage: String): ParsedResults = {
    val decodedLogs = decodeEvent(encodedMessage)
    implicit val messages: Seq[String] = decodedLogs.logEvents.map(_.message)
    log.debug(s"Decoding errors $errors")
    log.debug(s"Decoding metrics $metrics")
    log.debug(s"Decoding all others $logs")
    ParsedResults(errors, metrics, logs)
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

  def metrics()(implicit messages: Seq[String]): Seq[LogMessage] = {
    messages.filter(_.startsWith(metricPrefix))
      .map(x => {
        x.stripPrefix(metricPrefix)
          .stripSuffix("\n")
          .jsonDecode[LogMessage](Rename.logMessage)
      })
  }

  def logs()(implicit messages: Seq[String]): Seq[LogMessage] = {
    messages.filter(x => !x.startsWith(errorPrefix) && !x.startsWith(metricPrefix))
      .map(x => {
        x.replaceFirst(logRegex, "")
          .stripSuffix("\n")
          .jsonDecode[LogMessage](Rename.logMessage)
      })
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

  case class ParsedResults(errors: Seq[LogErrorMessage], metrics: Seq[LogMessage], logMessages: Seq[LogMessage])
}
