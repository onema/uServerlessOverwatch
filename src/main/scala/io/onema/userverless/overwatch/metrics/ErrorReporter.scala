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

package io.onema.userverless.overwatch.metrics

import com.amazonaws.services.sns.AmazonSNS
import com.typesafe.scalalogging.Logger
import io.onema.userverless.model.Log.{LogErrorMessage, Rename}
import io.onema.json.Extensions._

class ErrorReporter(val snsClient: AmazonSNS, val snsErrorTopic: Option[String]) {
  //--- Fields ---
  val log = Logger(classOf[ErrorReporter])

  //--- Methods ---
  def submit(error: LogErrorMessage): Unit = {
    if(error.reportException) {
      log.debug(s"""Publishing error error "${error.message}" """)
      snsErrorTopic.foreach(snsClient.publish(_, error.asJson(Rename.errorMessage)))
    } else {
      log.info(s"""SKIPPING Error error "${error.message}" as it has been marked as ignored""")
    }
  }
}
