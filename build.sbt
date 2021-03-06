
lazy val root = (project in file("."))
  .settings(
    organization := "io.onema",

    name := "userverless-overwatch",

    version := "0.3.0",

    scalaVersion := "2.12.8",

    libraryDependencies ++= {
      val awsSdkVersion = "1.11.510"
      Seq(
        "io.onema"                  % "userverless-core_2.12"      % "0.3.0",
        "com.amazonaws"             % "aws-java-sdk-logs"          % awsSdkVersion,
        "com.amazonaws"             % "aws-java-sdk-lambda"        % awsSdkVersion,
        "com.amazonaws"             % "aws-java-sdk-cloudwatch"    % awsSdkVersion,
        "com.amazonaws"             % "aws-java-sdk-sns"           % awsSdkVersion,

        // Logging
        "com.typesafe.scala-logging"% "scala-logging_2.12"        % "3.7.2",
        "ch.qos.logback"            % "logback-classic"           % "1.1.7",

        // Testing
        "org.scalatest"             %% "scalatest"                          % "3.0.4"   % Test,
        "org.scalamock"             % "scalamock-scalatest-support_2.12"    % "3.6.0"   % Test
      )
    }
  )
//  .dependsOn(uServerless)

//lazy val uServerless = (project in file("../uServerless"))

// Assembly
assemblyJarName in assembly := "app.jar"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")

