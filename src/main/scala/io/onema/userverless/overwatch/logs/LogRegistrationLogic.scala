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

import com.amazonaws.services.logs.AWSLogs
import com.amazonaws.services.logs.model.{PutRetentionPolicyRequest, PutSubscriptionFilterRequest}
import com.typesafe.scalalogging.Logger

class LogRegistrationLogic(val logClient: AWSLogs) {

  //--- Fields ---
  val log = Logger(classOf[LogRegistrationLogic])
  val filterPattern: String = """?"*** DEBUG" ?"*** INFO" ?"*** WARN" ?"ERROR" ?"*** METRIC" """

  //--- Methods ---
  def updateRetentionPolicy(logGroup: String, retentionTime: Int): Unit = {
    log.info(s"""Setting the retention policy for "$logGroup" to $retentionTime days.""")
    val request = new PutRetentionPolicyRequest(logGroup, retentionTime)
    logClient.putRetentionPolicy(request)
  }

  def updateSubscriptionFilter(logGroup: String, functionArn: String): Unit = {
    val request = new PutSubscriptionFilterRequest()
      .withFilterName("µserverless-logs")
      .withDestinationArn(functionArn)
      .withLogGroupName(logGroup)
      .withFilterPattern(filterPattern)
    logClient.putSubscriptionFilter(request)
  }

}
