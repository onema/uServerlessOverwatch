
lazy val root = (project in file("."))
  .settings(
    organization := "io.onema",

    name := "userverless-overwatch",

    version := "0.4.1",

    scalaVersion := "2.13.1",

    libraryDependencies ++= {
      val awsSdkVersion = "1.11.510"
      val awsSdkVersion2 = "2.5.1"
      Seq(
        "io.onema"                  %% "userverless-core"         % "0.4.1",
        "com.amazonaws"             % "aws-java-sdk-logs"         % awsSdkVersion,
        "com.amazonaws"             % "aws-java-sdk-lambda"       % awsSdkVersion,
        "com.amazonaws"             % "aws-java-sdk-sns"          % awsSdkVersion,
        "org.scala-lang.modules"    %% "scala-async"              % "0.10.0",
        

        // Logging
        "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.2",
        "ch.qos.logback"             %  "logback-classic"         % "1.1.7",

        // Testing
        "org.scalatest"             %% "scalatest"                % "3.1.1"   % Test,
        "org.scalamock"             %% "scalamock"                % "4.4.0"   % Test

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

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
