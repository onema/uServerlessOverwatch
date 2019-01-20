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

import com.amazonaws.services.lambda.AWSLambda
import com.amazonaws.services.lambda.model.{DeadLetterConfig, UpdateFunctionConfigurationRequest}
import com.typesafe.scalalogging.Logger

class QueueRegistrationLogic(val client: AWSLambda, val dqlArn: String) {

  //--- Fields ---
  val log = Logger(classOf[LogRegistrationLogic])

  //--- Methods ---
  def updateDql(function: String): Unit = {
    val request = new UpdateFunctionConfigurationRequest()
      .withFunctionName(function)
      .withDeadLetterConfig(new DeadLetterConfig().withTargetArn(dqlArn))
    client.updateFunctionConfiguration(request)
  }
}
