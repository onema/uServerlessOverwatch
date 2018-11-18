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


import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.overwatch.metrics.{ErrorReporter, MetricReporter}


class ParserFunction extends LambdaHandler[CloudWatchLogsEvent, Unit] with EnvLambdaConfiguration {

  //--- Fields ---
  private val snsErrorTopic = getValue("/sns/error/topic")
  private val metricReporter = new MetricReporter(AmazonCloudWatchClientBuilder.defaultClient())
  private val errorReporter = new ErrorReporter(AmazonSNSClientBuilder.defaultClient(), snsErrorTopic)

  //--- Methods ---
  override def execute(event: CloudWatchLogsEvent, context: Context): Unit = {
    val base64Event = event.getAwsLogs.getData
    val parsedResults = ParserLogic.parse(base64Event)

    log.debug(s"Submitting ${parsedResults.metrics.length} metrics")
    parsedResults.metrics.foreach(metricReporter.submit)

    log.debug(s"Submitting ${parsedResults.errors.length} errors")
    parsedResults.errors.foreach(errorReporter.submit)
  }
}
