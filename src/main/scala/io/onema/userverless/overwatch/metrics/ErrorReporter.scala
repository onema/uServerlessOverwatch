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

class ErrorReporter(val snsClient: AmazonSNS, val snsErrorTopic: Option[String]) {

  def submit(message: String): Unit = {
    snsErrorTopic.foreach(snsClient.publish(_, message))
  }
}
