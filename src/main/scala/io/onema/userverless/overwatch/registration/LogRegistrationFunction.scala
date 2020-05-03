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
  val destinationFunc: Option[String] = getValue("destination/function")
  val logGroupPrefix: String = getValue("log/group/prefix").getOrElse("/aws/lambda")
  val logic: LogRegistrationLogic = new LogRegistrationLogic(client, logGroupPrefix)

  //--- Methods ---
  override def execute(event: CloudWatchLogEvent, context: Context): Unit = {

    // Update retention policy of the newly created log group
    event.detail.requestParameters match {
      case Some(rp) =>
        val logGroup = rp.logGroupName
        logic.updateRetentionPolicy(logGroup, retentionTime)

        val accountId = context.accountId.getOrElse("")
        destinationFunc.foreach(func => logic.subscribe(func, accountId, logGroup, stageName, region))
      case None =>
        log.warn(s"Registration was not successful as the `detail.requestParameters` is null. Message will be ignored. Error: ${event.detail.errorMessage.getOrElse("No error available")}")
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
