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

package io.onema.userverless.overwatch.metrics

import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.{Dimension, MetricDatum, PutMetricDataRequest}
import com.typesafe.scalalogging.Logger
import io.onema.userverless.model.Log.LogMessage
import io.onema.userverless.overwatch.metrics.MetricReporter._

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class MetricReporter(val client: AmazonCloudWatch) {

  //--- Fields ---
  val log = Logger(classOf[MetricReporter])

  //--- Methods ---
  def submit(logMessage: LogMessage): Unit = {
    Try(parseMetric(logMessage.message)) match {
      case Success(metric) => putMetric(logMessage, metric)
      case Failure(ex) =>
        // Log the error, but don't fail as we may have other metrics to put
        log.error(ex.getMessage)
    }
  }

  def putMetric(logMessage: LogMessage, metric: Metric): Unit = {
    log.debug(s"""About to put metric "$metric"""")
    val dimensions = metric.tagMap.map {case (k, v) => new Dimension().withName(k).withValue(v)}.toList.asJava
    val namespace = toPascalCase(logMessage.function)
    val datum = new MetricDatum()
      .withMetricName(metric.name)
      .withUnit(metric.unit)
      .withValue(metric.value)
      .withDimensions(dimensions)
    val request = new PutMetricDataRequest()
      .withNamespace(s"$namespace")
      .withMetricData(datum)
    client.putMetricData(request)
  }
}

object MetricReporter {
  val log = Logger(classOf[MetricReporter])
  //--- Methods ---
  def parseMetric(metric: String): Metric = {
    log.info(s"PARSING: $metric")
    val metricSection :: tags :: _= metric.split("#").toList
    val name :: metrics :: _ = metricSection.split(":").toList
    val value :: metricType :: sampleRate :: _ = metrics.split("\\|").toList
    val tagMap = tags
      .split(":|,") // Split by ':' or ','
      .grouped(2) // Make groups of two
      .map { case Array(k, v) => k -> v } // make key value pairs and transform it to a map
      .toMap
    Metric(name, value.toDouble, metricType, sampleRate, tagMap)
  }

  def toPascalCase(value: String): String = {
    value.split("-|_|\\.|\\s").map(x => x.head.toUpper + x.tail).mkString("")
  }

  case class Metric(name: String, value: Double, metricType: String, sampleRate: String, tagMap: Map[String, String]) {
    def unit: String = metricType match {
      case "c" => "Count"
      case "s" => "Seconds"
      case "ms" => "Milliseconds"
      case "us" => "Microseconds"
      case notSupportedMetric: String => throw new RuntimeException(s"The metric '$notSupportedMetric' is not supported")
    }

    override def toString: String = {
      val tags = tagMap.map {case(k, v) => s"$k:$v"}.mkString(",")
      s"$name:$value|$value|$sampleRate|#$tags"
    }
  }
}
