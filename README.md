# µServerless Overwatch
Application to track and report metrics, and errors for applications build on top of µServerless.

## Overview
The "Overwatch" is a µServerless application that generates bootstrap infrastructure to enable you to monitor and 
report information about your µServerless lambda functions. 

The bootstrap infrastructure includes sns topics, sqs queues (DLQ), a cloud trail, and a few lambda functions. 

The overwatch listens for the creation of lambda cloudwatch logs and subscribes them (via the log registration functionArn)
to the log parser functionArn. The parser, parses the logs and gets the different log levels, errors and metrics reported by the 
framework.

![infrastructure](/docs/img/uServerlessOverwatch.png)

## Install

Using SBT generate the `.jar`
```bash
sbt assembly
```

and deploy with the serverless framework
```bash
serverless deploy
```
