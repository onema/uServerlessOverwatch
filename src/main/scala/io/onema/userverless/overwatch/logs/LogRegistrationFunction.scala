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
import com.amazonaws.services.logs.{AWSLogs, AWSLogsClientBuilder}
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.events.LogRegistration.LogCreationEvent
import io.onema.userverless.function.Extensions._
import io.onema.userverless.function.LambdaHandler
import io.onema.json.Extensions._
import org.json4s.{FieldSerializer, Formats, NoTypeHints}
import org.json4s.FieldSerializer._
import org.json4s.jackson.Serialization

class LogRegistrationFunction extends LambdaHandler[LogCreationEvent, Unit] with EnvLambdaConfiguration {

  //--- Fields ---
  val client: AWSLogs = AWSLogsClientBuilder.defaultClient()
  val retentionTime: Int = getValue("log/retention/days").getOrElse("7").toInt
  val logic: LogRegistrationLogic = new LogRegistrationLogic(client)
  val destinationFunc: Option[String] = getValue("destination/function")
  val logGroupPrefix: String = getValue("log/group/prefix").getOrElse("/aws/lambda")

  //--- Methods ---
  override def execute(event: LogCreationEvent, context: Context): Unit = {

    // Update retention policy of the newly created log group
    val logGroup = event.detail.requestParameters.logGroupName
    logic.updateRetentionPolicy(logGroup, retentionTime)

    // Get own self log group to avoid self registration
    val accoutnId = context.accountId.getOrElse("")
    destinationFunc.foreach(x => subscribe(x, accoutnId, logGroup))
  }

  def subscribe(destinationFunc: String, accountId: String, logGroup: String): Unit = {
    val destinationFuncArn = s"arn:aws:lambda:$region:$accountId:function:$destinationFunc"

    // Ignore the destination function itself to avoid invocation loops and logs that do not use the configured prefix
    if(logGroup == s"$logGroupPrefix/$destinationFunc" || !logGroup.startsWith(logGroupPrefix)) {
      log.info(s"""Ignoring the destination function $destinationFuncArn.""")
    } else {
      logic.updateSubscriptionFilter(logGroup, destinationFuncArn)
    }
  }

  override def jsonDecode(json: String): LogCreationEvent = {
    def fieldRenames: Formats = {
      Serialization.formats(NoTypeHints) +
        FieldSerializer[LogCreationEvent](
          renameTo("detailType", "detail-type"),
          renameFrom("detail-type", "detailType")
        )
    }
    json.jsonDecode[LogCreationEvent](fieldRenames)
  }
}
