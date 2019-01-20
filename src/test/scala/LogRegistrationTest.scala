import com.amazonaws.services.logs.AWSLogs
import io.onema.userverless.events.CloudTrailCloudWatchEvent.CloudWatchLogEvent
import io.onema.userverless.overwatch.registration.{LogRegistrationFunction, LogRegistrationLogic}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

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

class LogRegistrationTest extends FlatSpec with Matchers with MockFactory {
  "A functionArn LogRegistrationFunction" should "decode payload" in {
    // Arrange
    class LRFTest extends LogRegistrationFunction {
      def doJsonDecode(json: String): CloudWatchLogEvent = jsonDecode(json)
    }
    val event = """{"version":"0","id":"64fbda4e-5992-2224-6f33-3c8efe447fbb","detail-type":"AWS API Call via CloudTrail","source":"aws.logs","account":"123456789012","time":"2018-11-14T04:49:37Z","region":"us-east-1","resources":[],"detail":{"eventVersion":"1.04","userIdentity":{"type":"IAMUser","principalId":"AIDAJ4W5SF5AANMKCKRVW","arn":"arn:aws:iam::123456789012:user/juant-development","accountId":"123456789012","accessKeyId":"ASIAJB2QWB2IWP2O4GGQ","userName":"juant-development","sessionContext":{"attributes":{"mfaAuthenticated":"false","creationDate":"2018-11-14T04:49:33Z"}},"invokedBy":"cloudformation.amazonaws.com"},"eventTime":"2018-11-14T04:49:37Z","eventSource":"logs.amazonaws.com","eventName":"CreateLogGroup","awsRegion":"us-east-1","sourceIPAddress":"cloudformation.amazonaws.com","userAgent":"cloudformation.amazonaws.com","requestParameters":{"logGroupName":"/aws/lambda/echoapi"},"responseElements":null,"requestID":"b0b70559-e7c8-11e8-bb2f-bd281a5f414c","eventID":"efb309eb-d05e-40ee-bac8-0ff6adfb8ee3","eventType":"AwsApiCall","apiVersion":"20140328"}}"""
    val func = new LRFTest()

    // Act
    val response = func.doJsonDecode(event)

    // Assert
    val detail = response.detail
    val groupName = detail.requestParameters.get.logGroupName
    groupName should be("/aws/lambda/echoapi")

  }

  "A functionArn LogRegistrationFunction" should "not allow subscription if it is an overwatch functionArn" in {
    // Arrange
    class LRFTest(logsClient: AWSLogs) extends LogRegistrationFunction {
      override val logic: LogRegistrationLogic = new LogRegistrationLogic(logsClient)
    }
    val logsClient  =  mock[AWSLogs]
    (logsClient.putSubscriptionFilter _).expects(*).never()
    val func = new LRFTest(logsClient)

    // Act - Assert
    func.subscribe("foo", "123456789012", "/aws/lambda/overwatch-foo-bar-baz")
  }
}
