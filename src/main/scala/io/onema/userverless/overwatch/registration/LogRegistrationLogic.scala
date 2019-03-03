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

import com.amazonaws.services.logs.AWSLogs
import com.amazonaws.services.logs.model.{PutRetentionPolicyRequest, PutSubscriptionFilterRequest}
import com.typesafe.scalalogging.Logger

class LogRegistrationLogic(val logClient: AWSLogs, val logGroupPrefix: String) {

  //--- Fields ---
  val log = Logger(classOf[LogRegistrationLogic])
  val filterPattern: String = """?"*** DEBUG" ?"*** INFO" ?"*** WARN" ?"*** ERROR" ?"*** METRIC" ?"REPORT" ?"Metaspace" ?"Task timed out""""

  //--- Methods ---
  def subscribe(destinationFunc: String, accountId: String, logGroup: String, stageName: String, region: String): Unit = {
    val destinationFuncArn = s"arn:aws:lambda:$region:$accountId:function:$destinationFunc"

    // Ignore the destination functionArn itself to avoid invocation loops and logs that do not use the configured prefix
    if(logGroup == s"$logGroupPrefix/$destinationFunc" || !logGroup.startsWith(logGroupPrefix) || logGroup.startsWith(s"/aws/lambda/overwatch") || !logGroup.contains(stageName)) {
      log.info(s"""Ignoring the destination functionArn $destinationFuncArn.""")
    } else {
      updateSubscriptionFilter(logGroup, destinationFuncArn)
    }
  }

  def updateRetentionPolicy(logGroup: String, retentionTime: Int): Unit = {
    log.info(s"""Setting the retention policy for "$logGroup" to $retentionTime days.""")
    val request = new PutRetentionPolicyRequest(logGroup, retentionTime)
    logClient.putRetentionPolicy(request)
  }

  def updateSubscriptionFilter(logGroup: String, functionArn: String): Unit = {
    log.info(s"""Setting subscription filter for "$logGroup" to "$functionArn" """)
    val request = new PutSubscriptionFilterRequest()
      .withFilterName("µserverless-logs")
      .withDestinationArn(functionArn)
      .withLogGroupName(logGroup)
      .withFilterPattern(filterPattern)
    logClient.putSubscriptionFilter(request)
  }
}
