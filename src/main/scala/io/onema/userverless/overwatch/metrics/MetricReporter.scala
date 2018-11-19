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
  def submit(namespace: String, logMessages: Seq[LogMessage]): Unit = {
    if(namespace == "APP_NAME_IS_UNDEFINED") {
      log.warn(s"""NAMESPACE "$namespace" IS NOT VALID AND WILL NOT BE REPORTED!""")
      return
    }
    val parsedMetrics = logMessages.map(m => {
      Try(parseMetric(m.message)) match {
        case Success(metric) => Some(metric)
        case Failure(ex) =>
          // Log the error, but don't fail
          log.error(ex.getMessage)
          None
      }
    }).filter(_.isDefined).flatten
    putMetrics(namespace, parsedMetrics)
  }

  def putMetrics(namespace: String, metrics: Seq[Metric]): Unit = {
    val ns = toPascalCase(namespace)

    // Create metric datum for each metric we are going to report, and batch it in groups of 20
    val metricDatum: Iterator[Seq[MetricDatum]] = metrics.map(metric => {
      log.debug(s"""About to put metric "$metric"""")
      val dimensions = metric.tagMap.map {
        case (k, v) => new Dimension().withName(k).withValue(v)
      }.toList.asJava
      new MetricDatum()
        .withMetricName(metric.name)
        .withUnit(metric.unit)
        .withValue(metric.value)
        .withDimensions(dimensions)
    }).grouped(20)

    // for each batch create and submit a new request
    metricDatum.foreach(x => {
      val request = new PutMetricDataRequest()
        .withNamespace(s"$ns")
      log.debug(s"BATCHED ${x.length} metrics for the namespace $ns")
      x.foreach(request.withMetricData(_))
      client.putMetricData(request)
    })
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
