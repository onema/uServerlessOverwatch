/**
  * This file is part of the ONEMA userverless-overwatch Package.
  * For the full copyright and license information,
  * please view the LICENSE file that was distributed
  * with this source code.
  *
  * copyright (c) 2019, Juan Manuel Torres (http://onema.io)
  *
  * @author Juan Manuel Torres <software@onema.io>
  */

package io.onema.userverless.overwatch.registration

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.runtime.Context
import io.onema.json.Extensions._
import io.onema.userverless.configuration.lambda.EnvLambdaConfiguration
import io.onema.userverless.events.CloudTrailLambdaEvent.LambdaEvent
import io.onema.userverless.function.LambdaHandler
import org.json4s.FieldSerializer.{renameFrom, renameTo}
import org.json4s.jackson.Serialization
import org.json4s.{FieldSerializer, Formats, NoTypeHints}

class QueueRegistrationFunction extends LambdaHandler[LambdaEvent, Unit] with EnvLambdaConfiguration {

  //--- Fields ---
  val dqlArn = getValue("dql/arn").getOrElse(throw new Exception("DQL_ARN configuration value is missing"))
  val logic = new QueueRegistrationLogic(AWSLambdaClientBuilder.defaultClient(), dqlArn)

  //--- Methods ---
  override def execute(event: LambdaEvent, context: Context): Unit = {
    event.detail.requestParameters match {
      case Some(rp) =>
        val functionName = rp.functionName.getOrElse(throw new Exception("The function name is missing and it is a required value to subscribe a DQL"))
        if(!functionName.contains(stageName)) {
          log.info(s"""Ignoring the destination functionArn $functionName.""")
        } else {
          logic.updateDql(functionName)
        }
      case None =>
        log.warn(s"Registration was not successful as the `detail.requestParameters` is null. Message will be ignored. Error: ${event.detail.errorMessage.getOrElse("No error available")}")
    }
  }

  override def jsonDecode(json: String): LambdaEvent = {
    def fieldRenames: Formats = {
      Serialization.formats(NoTypeHints) +
        FieldSerializer[LambdaEvent](
          renameTo("detailType", "detail-type"),
          renameFrom("detail-type", "detailType")
        )
    }
    json.jsonDecode[LambdaEvent](fieldRenames)
  }
}
