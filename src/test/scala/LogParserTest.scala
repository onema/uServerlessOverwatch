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

import io.onema.userverless.model.Log
import io.onema.userverless.overwatch.logs.ParserLogic
import io.onema.userverless.overwatch.metrics.MetricReporter
import org.scalatest.{FlatSpec, Matchers}


class LogParserTest extends FlatSpec with Matchers {

  val errorPrefix = "*** ERROR :"
  val metricPrefix = "*** METRIC :"
  val logRegex = "[\\*]+ [A-Z\\s]+:"

  "Log Parser Function" should "decode the log payload" in {
    // Arrange
    val base64EncodedEvent = "H4sIAAAAAAAAAH1SS27bMBS8SsF25epDSvInXMVolCBF3QKxdpFh0BKrEJBIg6QcFLaPlQv0ZH2ipKiraCUMZ+bNG/KMGm4Mq3j258gRRXfrbL3fpNvt+iFFHlKvkmuASRQn88VydYNJBHCtqget2iOchOzVhDVrDiULefGi2FH4JT/54//vVhZWKNmrtlZz1oAswmQVEhKSefj85cc6S7fZjhwiBjTTHkyhxbFT3Yvacm0QfUZ/3wzXJ65ryOuDlUE7Z5meuLQd44xECc5xEt/Ei3gJTlbAcpY1kJPMkyhaJDjG8WrpjUsDfTabfdqk2dPjN3rO0e27JEc0dyl9Qnwyz/CCJpjGOAD9V4wpxjnyQACJDAR1dOKgwdsh342Sd7xQJYcE0aUxl1ty+Tx2Qj8qzBuc6VCPB7EqToHqpsDmFdd7yZp+klCBkrxhQTvVFDRKCqu0kFWw4VaLwjitfYFbKCdtw4TsTfmJ1w56/Hn/a4L2J1a3HTfC8LkdO7fJYdpzqMAdW3hT7rhrtZ88Ij3DYW4vB467jRU48KOO+oTu8e3/v4ihshxdc4muu+s/oF0KSOYCAAA="

    // Act
    val response = ParserLogic.decodeEvent(base64EncodedEvent)

    // Assert
    response.messageType.get should be("DATA_MESSAGE")

  }

  "Log Parser Function" should "decode another payload" in {
    // Arrange
    val base64EncodedEvent = "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"

    // Act
    val response = ParserLogic.decodeEvent(base64EncodedEvent)
    val logEvents = response.logEvents.map(_.message)
    val errors: Seq[String] = ParserLogic.errors()(logEvents)
    val metrics: Seq[Log.LogMessage] = ParserLogic.metrics()(logEvents)
    val logs: Seq[Log.LogMessage] = ParserLogic.logs()(logEvents)

    // Assert
    response.logEvents.length should be (9)
    errors.length should be (2)
    metrics.length should be (4)
    logs.length should be (3)
  }

  "Metric Parser " should "decode a metric" in {
    // Arrange
    val rawMetric = "WarmUpEvent:200|ms|@1|#function:overwatch-dev-log_parser,version:$LATEST,stage:dev"

    // Act
    val metric = MetricReporter.parseMetric(rawMetric)

    // Assert
    metric.name should be ("WarmUpEvent")
    metric.value should be (200)
    metric.sampleRate should be ("@1")
    metric.metricType should be ("ms")
    metric.tagMap("function") should be ("overwatch-dev-log_parser")
    metric.tagMap("version") should be ("$LATEST")
    metric.tagMap("stage") should be ("dev")
  }

  "Metric Parser " should "decode a metric with namespace" in {
    // Arrange
    val rawMetric = "foo.warmup.event:200|ms|@1|#function:overwatch-dev-log_parser,version:$LATEST,stage:dev"

    // Act
    val metric = MetricReporter.parseMetric(rawMetric)

    // Assert
    metric.name should be ("foo.warmup.event")
    metric.value should be (200)
    metric.sampleRate should be ("@1")
    metric.metricType should be ("ms")
    metric.tagMap("function") should be ("overwatch-dev-log_parser")
    metric.tagMap("version") should be ("$LATEST")
    metric.tagMap("stage") should be ("dev")
  }

  "Conversion to pascal case " should "be correct" in {
    // Arrange
    val name = "foo.warmup.event"

    // Act
    val result = MetricReporter.toPascalCase(name)

    // Assert
    result should be ("FooWarmupEvent")
  }

  "Metric Parser " should "decode a metric with additional values" in {
    // Arrange
    val rawMetric = "ForwardingEmail:1|c|@1|#function:lambda-mailer-dev-forwarder,version:$LATEST,stage:dev,from:Juan Manuel Torres <myemail@yahoo.com>"

    // Act
    val metric = MetricReporter.parseMetric(rawMetric)

    // Assert
    metric.name should be ("ForwardingEmail")
    metric.value should be (1)
    metric.sampleRate should be ("@1")
    metric.metricType should be ("c")
    metric.tagMap("function") should be ("lambda-mailer-dev-forwarder")
    metric.tagMap("version") should be ("$LATEST")
    metric.tagMap("stage") should be ("dev")
    metric.tagMap("from") should be ("Juan Manuel Torres <myemail@yahoo.com>")
  }
}
