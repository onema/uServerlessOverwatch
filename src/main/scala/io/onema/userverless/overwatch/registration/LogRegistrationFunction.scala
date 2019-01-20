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

package io.onema.userverless.overwatch.registration

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.logs.{AWSLogs, AWSLogsClientBuilder}
import io.onema.json.Extensions._
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.events.CloudTrailCloudWatchEvent.CloudWatchLogEvent
import io.onema.userverless.function.Extensions._
import io.onema.userverless.function.LambdaHandler
import org.json4s.FieldSerializer._
import org.json4s.jackson.Serialization
import org.json4s.{FieldSerializer, Formats, NoTypeHints}

class LogRegistrationFunction extends LambdaHandler[CloudWatchLogEvent, Unit] with EnvLambdaConfiguration {

  //--- Fields ---
  val client: AWSLogs = AWSLogsClientBuilder.defaultClient()
  val retentionTime: Int = getValue("log/retention/days").getOrElse("7").toInt
  val logic: LogRegistrationLogic = new LogRegistrationLogic(client)
  val destinationFunc: Option[String] = getValue("destination/function")
  val logGroupPrefix: String = getValue("log/group/prefix").getOrElse("/aws/lambda")

  //--- Methods ---
  override def execute(event: CloudWatchLogEvent, context: Context): Unit = {

    // Update retention policy of the newly created log group
    event.detail.requestParameters match {
      case Some(rp) =>
        val logGroup = rp.logGroupName
        logic.updateRetentionPolicy(logGroup, retentionTime)

        // Get own self log group to avoid self registration
        val accountId = context.accountId.getOrElse("")
        destinationFunc.foreach(x => subscribe(x, accountId, logGroup))
      case None =>
        log.warn(s"Registration was not successful as the `detail.requestParameters` is null. Message will be ignored. Error: ${event.detail.errorMessage.getOrElse("No error available")}")
    }
  }

  def subscribe(destinationFunc: String, accountId: String, logGroup: String): Unit = {
    val destinationFuncArn = s"arn:aws:lambda:$region:$accountId:functionArn:$destinationFunc"

    // Ignore the destination functionArn itself to avoid invocation loops and logs that do not use the configured prefix
    if(logGroup == s"$logGroupPrefix/$destinationFunc" || !logGroup.startsWith(logGroupPrefix) || logGroup.startsWith(s"/aws/lambda/overwatch") || !logGroup.contains(stageName)) {
      log.info(s"""Ignoring the destination functionArn $destinationFuncArn.""")
    } else {
      logic.updateSubscriptionFilter(logGroup, destinationFuncArn)
    }
  }

  override def jsonDecode(json: String): CloudWatchLogEvent = {
    def fieldRenames: Formats = {
      Serialization.formats(NoTypeHints) +
        FieldSerializer[CloudWatchLogEvent](
          renameTo("detailType", "detail-type"),
          renameFrom("detail-type", "detailType")
        )
    }
    json.jsonDecode[CloudWatchLogEvent](fieldRenames)
  }
}
