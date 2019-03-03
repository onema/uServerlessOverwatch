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


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.monitoring.LogMetrics._


class ParserFunction extends LambdaHandler[CloudWatchLogsEvent, Unit] with EnvLambdaConfiguration {

  //--- Fields ---
  private val snsErrorTopic = getValue("/sns/error/topic")
  private val snsNotificationTopic = getValue("/sns/notification/topic")
  private val snsLogTopic = getValue("/sns/log/topic")
  private val snsMetricTopic = getValue("/sns/metric/topic")
  private val reporter = new Reporter(AmazonSNSClientBuilder.defaultClient(), snsErrorTopic, snsNotificationTopic, snsLogTopic, snsMetricTopic)

  //--- Methods ---
  override def execute(event: CloudWatchLogsEvent, context: Context): Unit = {
    val parsedResults: ParserLogic.ParsedResults = time("ParseLogs") {
      val base64Event = event.getAwsLogs.getData
      ParserLogic.parse(base64Event)
    }

    time("MetricsReport") {
      log.debug(s"Submitting ${parsedResults.metrics.length} metrics")
      reporter.metric(parsedResults.metrics)
    }

    time("ErrorReport") {
      log.debug(s"Submitting ${parsedResults.errors.length} errors")
      parsedResults.errors.foreach(reporter.error)
    }

    time("MetaspaceErrorReport") {
      log.debug(s"Submitting ${parsedResults.metaspaceErrors.length} Metaspace errors")
      parsedResults.metaspaceErrors.foreach(reporter.error)
    }

    time("TimeOutErrorReport") {
      log.debug(s"Submitting ${parsedResults.timeoutError.length} timeout error")
      parsedResults.timeoutError.foreach(reporter.timeout)
    }

    time("NotificationsReport") {
      log.debug(s"Submitting ${parsedResults.report.length} reports")
      parsedResults.report.foreach(reporter.report(_, parsedResults.functionName))
    }

    time("LogReport") {
      log.debug(s"Submitting ${parsedResults.logMessages.length} logs")
      reporter.log(parsedResults.logMessages)
    }
  }
}
