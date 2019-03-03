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

import io.onema.json.Extensions._
import io.onema.userverless.model.{Log, Metric}
import io.onema.userverless.overwatch.logs.ParserLogic
import io.onema.userverless.overwatch.logs.ParserLogic._
import org.scalatest.{FlatSpec, Matchers}


class LogParserTest extends FlatSpec with Matchers {

  val errorPrefix = "*** ERROR :"
  val metricPrefix = "*** METRIC :"
  val reportPrefix = "REPORT"
  val logRegex = "[\\*]+ [A-Z\\s]+:"

  "Log Parser Function" should "decode the log payload" in {
    // Arrange
    val base64EncodedEvent = "H4sIAAAAAAAAAH1SS27bMBS8SsF25epDSvInXMVolCBF3QKxdpFh0BKrEJBIg6QcFLaPlQv0ZH2ipKiraCUMZ+bNG/KMGm4Mq3j258gRRXfrbL3fpNvt+iFFHlKvkmuASRQn88VydYNJBHCtqget2iOchOzVhDVrDiULefGi2FH4JT/54//vVhZWKNmrtlZz1oAswmQVEhKSefj85cc6S7fZjhwiBjTTHkyhxbFT3Yvacm0QfUZ/3wzXJ65ryOuDlUE7Z5meuLQd44xECc5xEt/Ei3gJTlbAcpY1kJPMkyhaJDjG8WrpjUsDfTabfdqk2dPjN3rO0e27JEc0dyl9Qnwyz/CCJpjGOAD9V4wpxjnyQACJDAR1dOKgwdsh342Sd7xQJYcE0aUxl1ty+Tx2Qj8qzBuc6VCPB7EqToHqpsDmFdd7yZp+klCBkrxhQTvVFDRKCqu0kFWw4VaLwjitfYFbKCdtw4TsTfmJ1w56/Hn/a4L2J1a3HTfC8LkdO7fJYdpzqMAdW3hT7rhrtZ88Ij3DYW4vB467jRU48KOO+oTu8e3/v4ihshxdc4muu+s/oF0KSOYCAAA="

    // Act
    val response = ParserLogic.decodeEvent(base64EncodedEvent)

    // Assert
    response.messageType.get should be("DATA_MESSAGE")

  }

  "Log Parser Function" should "decode another payload" in {
    // Arrange
    val base64EncodedEvent = "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"
    val functionName = "appName"

    // Act
    val response = ParserLogic.decodeEvent(base64EncodedEvent)
    val logEvents = response.logEvents.map(_.message)
    val errors: Seq[Log.LogErrorMessage] = ParserLogic.getErrors(logEvents)
    val metrics: Seq[Metric] = ParserLogic.getMetrics(logEvents)
    val logs: Seq[Log.LogMessage] = ParserLogic.getLogs(logEvents)

    // Assert
    response.logEvents.length should be (9)
    errors.length should be (2)
    metrics.length should be (4)
    logs.length should be (3)
    metrics.head.appName should be  ("test")
  }

  "Log Parser Function " should "parse function report" in {
    // Act
    val base64EncodedEvent = "H4sIAAAAAAAAAE2QQW7bMBBFr0IQXUbVDCWSoncOogYFarSw1FVsFJRIBwIkyyXpuGmQY/UCOVlHRgp0ReL/+fPw54VPPkb76Nvnk+crfrdu1z82ddOs72t+w+fL0QeSQUmUUClADSSP8+N9mM8ncnJ7iflop87Z9yeb7DD6kCUfU3aYw8UGR0uuqSYFbyeKCUCTA+YC8ocPX9Zt3bR72zunfVVpPPSl6VwldeeUwBKllEot4HjuYh+GUxrm46dhTD5Evnrgb3+iD08+jNQlI0zk+yuufvLHtEy88MERtSilQCioilEGBIDBCozURppCLX9NHFRKGVmoUmBhtDYkEjkNdKhkJ+qMstRGK2kAhbr5d0Bav62/fd22bOt/nmn2s1sx2/fCFocio41dVnYVZgYPKlNY2r4reugN7tLdOdil0YohKPwIwKa4S7fDOHrH/jPx6rBd2vhpDs+sGX77JSNKtrkl1f5i78736ImOwlyN3ZG/7l//AmxFaBfrAQAA"
    val response = ParserLogic.decodeEvent(base64EncodedEvent)
    val logEvents = response.logEvents.map(_.message)
    val report = ParserLogic.getReport(logEvents)

    // Assert
    response.logEvents.length should be (1)
    report.length should be (1)
    report.head.duration.value should be ("1061.00")
    report.head.duration.reportUnit should be ("ms")
    report.head.billedDuration.value should be ("1100")
    report.head.billedDuration.reportUnit should be ("ms")
    report.head.memorySize.value should be ("1024")
    report.head.memorySize.reportUnit should be ("MB")
    report.head.maxMemoryUsed.value should be ("129")
    report.head.maxMemoryUsed.reportUnit should be ("MB")
  }

  "Log Parser Function " should "parse function report2" in {
    // Act
    val base64EncodedEvent = "H4sIAAAAAAAAAK2Rz24TMRDGX2W14hg3Y3v8Z3JL1VAhEYGScGoq5KydaqXdJKydllL1sXgBnoxJCQIkJC6cxjPfzHz6jZ/qPuUc7tLq8ZDqSX01XU0/zmfL5fR6Vo/q/cMuDVwGa6QBb0E64HK3v7se9scDK+PwkMdd6DcxnIPoQ9ulQZSUy/n9Y2RZhhR6nlEgaQxyrGB88+rtdDVbrm632kMTcdtYv8FoTLDOEnok1Fo7lLwiHze5GdpDafe7121X0pDryU397WtOw30aOgYRbJPr2xe72X3alVPHU91GdtVolARDxPsUoUTjTkTaETkAkspKS95KIkWG0BjQRpNy7FxavlIJPQNLg46c8xYd4Ojn9Xj9Yvb+3WJVLdKnI/e+iZPKek/RkxPKxyhw00QRGgjCSHSnLKiI63J1HMKJiPvthVZVn9flsu26FKtfkgRgoVqXeer3w2O1bL+kU1VhNb/kavhcnZUPObG1tPAirHf18+gPfgtKGwI0kn8BiCE55xsA70JFGsAo5YED357Aaft3fueV+Qd/0C6hl05g4xqBPm1FwOgFKCddaKyUin7nR7pQ7v/z3z5/By/yz1zlAgAA"
    val response = ParserLogic.decodeEvent(base64EncodedEvent)
    val logEvents = response.logEvents.map(_.message)
    val report = ParserLogic.getReport(logEvents)

    // Assert
    response.logEvents.length should be (2)
  }

  "Metric Parser " should "decode a metric" in {
    // Arrange
    val rawMetric = "WarmUpEvent:200|ms|@1|#functionArn:overwatch-dev-log_parser,version:$LATEST,stage:dev"

    // Act
    val metric = ParserLogic.parseMetric(rawMetric, "testFunction")

    // Assert
    metric.name should be ("WarmUpEvent")
    metric.value should be (200)
    metric.sampleRate should be ("@1")
    metric.metricType should be ("ms")
    metric.tagMap("functionArn") should be ("overwatch-dev-log_parser")
    metric.tagMap("version") should be ("$LATEST")
    metric.tagMap("stage") should be ("dev")
  }

  "Metric Parser " should "decode a metric with namespace" in {
    // Arrange
    val rawMetric = "foo.warmup.event:200|ms|@1|#functionArn:overwatch-dev-log_parser,version:$LATEST,stage:dev"

    // Act
    val metric = ParserLogic.parseMetric(rawMetric, "testFunction")

    // Assert
    metric.name should be ("foo.warmup.event")
    metric.value should be (200)
    metric.sampleRate should be ("@1")
    metric.metricType should be ("ms")
    metric.tagMap("functionArn") should be ("overwatch-dev-log_parser")
    metric.tagMap("version") should be ("$LATEST")
    metric.tagMap("stage") should be ("dev")
  }

  "Metric Parser " should "decode a metric with additional values" in {
    // Arrange
    val rawMetric = "ForwardingEmail:1|c|@1|#functionArn:lambda-mailer-dev-forwarder,version:$LATEST,stage:dev,from:Juan Manuel Torres <myemail@yahoo.com>"

    // Act
    val metric = ParserLogic.parseMetric( rawMetric, "testFunction")

    // Assert
    metric.name should be ("ForwardingEmail")
    metric.value should be (1)
    metric.sampleRate should be ("@1")
    metric.metricType should be ("c")
    metric.tagMap("functionArn") should be ("lambda-mailer-dev-forwarder")
    metric.tagMap("version") should be ("$LATEST")
    metric.tagMap("stage") should be ("dev")
    metric.tagMap("from") should be ("Juan Manuel Torres <myemail@yahoo.com>")
    metric.appName should be ("testFunction")
  }

  "Report Parser " should "decode a report with error errorMessage" in {
    // Arrange
    val plainText = "REPORT RequestId: 73443423-093f-11e9-89f0-8773e4a516b5\tDuration: 183.11 ms\tBilled Duration: 200 ms Memory Size: 1024 MB\tMax Memory Used: 145 MB\t"
    val logs = Logs(None, None, None, None, None, List(LogEvents(None, None, plainText))).asJson
    val  encoded = ParserLogic.encodeEvent(logs)

    // Act
    val results = ParserLogic.parse(encoded)
    val report = results.report.head

    // Assert
    results.report.length should be (1)
    report.requestId should be ("73443423-093f-11e9-89f0-8773e4a516b5")
    report.duration.value should be ("183.11")
    report.duration.reportUnit should be ("ms")

    report.billedDuration.value should be ("200")
    report.billedDuration.reportUnit should be ("ms")

    report.memorySize.value should be ("1024")
    report.memorySize.reportUnit should be ("MB")

    report.maxMemoryUsed.value should be ("145")
    report.maxMemoryUsed.reportUnit should be ("MB")
  }

  "Log parser" should "parse out of memory exceptions" in {
    // Arrange
    val base64EncodedEvent = "H4sIAAAAAAAAALWVbU/bMBDHvwqKeMEkcGPnuUKTOihoUguIsleAkEmuwVNiV7bTiiG++y5Jgba0gDpNipT47P/vzufz5ckpwRiew9XjBJyuc9y76t0N+6NR77Tv7DtqJkGjmTLPD8IoTlzK0Fyo/FSraoIzHT4znYKX9xmfvw5KLgrQBxaMnX+3kpHVwEvUMJcmHdfruKxzvTvoXfVHV7exx2gGXkQjL/HHYzdmNEjYPYtTFjHue4gw1b1JtZhYoeSJKCxo43SvnUHjtYXfqSnoGbfpQ+sevR5MuDYYwm0TQ38K0tayJ0dkGIrnh66bUBrh9nwauRHuM46SkLEwjAM/YV4S0DD0o4SGXhz7fuxGQRSFGI4VmDrLS8wCDQJ8cL2HKdp/SSnih2C5mfAUuju/+ZSTgsucnFf2fDyEUunHvtZK38jNc92dV8SNvLHcLnCOCm7MQPEMNMlgLCQ0Frp3xq2YQq18UNm3r+r2Fu316m4UektqA2mlhX0ko/oDNnHezzY06rMlmgRLfl0ONlFWphqEH0afIXiaYvZ3qeuuJUTeJ4BdSnQl12q9MP4XMVufy14T8JGSVquiqJOgLrSY4rXJIfvgKNfsHZOXbU6eF9KPa6HA14ZK8Jm/tdYLog1aMlb6jJfgfqliX1a3HuZs//VIlM4Jx3vyACRVZamkwaDyXKBclJOCDFR+wlOLF+tnPUyxXViojVqVbeQrKxoHSbKtg0yYtO5GaK7HUGLj4XXrWusHO8uWfiTMfkrsQzKFteTAp1uSc7AfklnC/hc5TL5AflPWQBwtsFpO+FZ6KCa85H+UxB8W0ZCLGtWbmcvm80KrqcCSPXrgQpLDtBBS2O97G+bb2vPWs1GLR03uK1HUdwARR43lx9ywCF+eaWN2X7BCESWh5KT9iZJh8zqpZFqXETlsKStWk/ICY3s9GVNJ3O24gNSS9pZhqzFWV3Wa2t6j9GohbbiQW7H2Pl7Z7nltuMeAPRDFMv+Km89Xt50seN9eXhwuCJfQi/Z5O2zOXjrPt89/AfJdaadCCQAA"
    val json = """{"messageType":"DATA_MESSAGE","owner":"123456789012","logGroup":"/aws/lambda/lambda-mailer-test-mailer","logStream":"2019/03/02/[$LATEST]8321de3717394ff0821592b28c272a43","subscriptionFilters":["LambdaStream_overwatch-test-log-parser"],"logEvents":[{"id":"34600911745641707345879622668549239516647916388448075776","timestamp":1551558543789,"message":"Metaspace: java.lang.OutOfMemoryError\njava.lang.OutOfMemoryError: Metaspace\n\tat java.lang.ClassLoader.defineClass1(Native Method)\n\tat java.lang.ClassLoader.defineClass(ClassLoader.java:763)\n\tat java.security.SecureClassLoader.defineClass(SecureClassLoader.java:142)\n\tat java.net.URLClassLoader.defineClass(URLClassLoader.java:467)\n\tat java.net.URLClassLoader.access$100(URLClassLoader.java:73)\n\tat java.net.URLClassLoader$1.run(URLClassLoader.java:368)\n\tat java.net.URLClassLoader$1.run(URLClassLoader.java:362)\n\tat java.security.AccessController.doPrivileged(Native Method)\n\tat java.net.URLClassLoader.findClass(URLClassLoader.java:361)\n\tat java.lang.ClassLoader.loadClass(ClassLoader.java:424)\n\tat java.lang.ClassLoader.loadClass(ClassLoader.java:357)\n\tat java.lang.Class.forName0(Native Method)\n\tat java.lang.Class.forName(Class.java:348)\n\tat org.apache.commons.logging.impl.LogFactoryImpl.createLogFromClass(LogFactoryImpl.java:998)\n\tat org.apache.commons.logging.impl.LogFactoryImpl.discoverLogImplementation(LogFactoryImpl.java:844)\n\tat org.apache.commons.logging.impl.LogFactoryImpl.newInstance(LogFactoryImpl.java:541)\n\tat org.apache.commons.logging.impl.LogFactoryImpl.getInstance(LogFactoryImpl.java:292)\n\tat org.apache.commons.logging.impl.LogFactoryImpl.getInstance(LogFactoryImpl.java:269)\n\tat org.apache.commons.logging.LogFactory.getLog(LogFactory.java:657)\n\tat com.amazonaws.regions.AwsRegionProviderChain.<clinit>(AwsRegionProviderChain.java:33)\n\tat com.amazonaws.client.builder.AwsClientBuilder.<clinit>(AwsClientBuilder.java:60)\n\tat io.onema.mailer.MailerFunction.<init>(MailerFunction.scala:32)\n\tat sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)\n\tat sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)\n\tat sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)\n\tat java.lang.reflect.Constructor.newInstance(Constructor.java:423)\n\n"}]}"""
    val encoded = ParserLogic.encodeEvent(json)

    // Act
    val result = ParserLogic.parse(base64EncodedEvent)

    // Assert
    result.metaspaceErrors.head.stackTrace.size should be (26)
    result.metaspaceErrors.head.message should be ("Metaspace: java.lang.OutOfMemoryError")
    result.metaspaceErrors.head.function should be ("lambda-mailer-test-mailer")
  }

  "Log parser" should "parse timeout errors" in {
    // Arrange
    val base64EncodedEvent = "H4sIAAAAAAAAAKVRyW7bMBD9FULo0Yo5FBdJNwdRgwIxWtjqpZERUCKdCtXiklTcNMi/Z2SnKHILEIAEh/NmefPmKeqt9/relo8HG+XR1apc3a2L7XZ1XUSLaDwO1qEbWMKFVGlGgaG7G++v3TgdEFnqo192uq+Nfn3iXreddfF+dEftDFrB+vD/e87fBmd1jwUYhWxJk/ncfrpZlcW23Km9aBhPa0615VYltVIKqGZggElLDZbwU+0b1x5COw6f2y5Y56P8Nro5UTgXvxsfLPYMzc8zA+waH7TzSGF34lA82CHMaU9Ra5BKwiUFKSjLGGNAFU9ZOl9IJZOpTARPM65SoRTHECVmGz0c6YQWdQy6R0lACBAZSAmJTBb/9MXym+Lb101JNvb3hLFfTE4aKkRd1zrmYLKYZ8k+rmnaxEZlUovMpEzLKlxNTs9j5gQohQugpPdVuGy7zhryBpwRUoW17Uf3SLbtX5sTAYysL9Gp/5BX4Lu32BxAnoBqiJ4XH1NAvFOBedcxTfCUVOQcchwnEfTHu4Qgpfa/yNzGkHEKRO9x6QQucGpvm3EwvhrmWXbPL16JbWvVAgAA"

    // Act
    val result = ParserLogic.parse(base64EncodedEvent)

    // Assert
    result.timeoutError.head.message should be ("Task timed out after 1.00 seconds")
  }
}

