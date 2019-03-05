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
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.function.LambdaHandler
import io.onema.userverless.monitoring.LogMetrics._
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.PublishResponse

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ParserFunction extends LambdaHandler[CloudWatchLogsEvent, Unit] with EnvLambdaConfiguration {

  //--- Fields ---
  private val snsErrorTopic = getValue("/sns/error/topic")
  private val snsNotificationTopic = getValue("/sns/notification/topic")
  private val snsLogTopic = getValue("/sns/log/topic")
  private val snsMetricTopic = getValue("/sns/metric/topic")
  private val snsClient = SnsAsyncClient.builder().httpClientBuilder(NettyNioAsyncHttpClient.builder()).build()
  private val reporter = new Reporter(snsClient, snsErrorTopic, snsNotificationTopic, snsLogTopic, snsMetricTopic)

  //--- Methods ---
  override def execute(event: CloudWatchLogsEvent, context: Context): Unit = {
    val parsedResults: ParserLogic.ParsedResults = time("ParseLogs") {
      val base64Event = event.getAwsLogs.getData
      ParserLogic.parse(base64Event)
    }

    time("PublishResults") {
      val future: Future[Seq[PublishResponse]] = reporter.publishResultsAsync(parsedResults)
      Await.ready(future, 10.seconds)
    }
  }
}
