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
    val errors: Seq[Log.LogErrorMessage] = ParserLogic.errors()(logEvents)
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
