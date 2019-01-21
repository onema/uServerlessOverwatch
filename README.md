# µServerless Overwatch
Application to subscribe new lambda functions logs to the overwatch log parser, and DLQ. The purpose of the overwatch is to
make available errors, logs, notifications and metrics to other applications via SNS topics.

## Overview
The "Overwatch" is a µServerless application that generates bootstrap infrastructure to enable you to monitor and 
report information about your µServerless lambda functions. 

The bootstrap infrastructure includes sns topics, sqs queues (DLQ), and a few lambda functions. 

The overwatch listens for the creation of lambda cloudwatch logs and subscribes them (via the log registration functionArn)
to the log parser functionArn. The parser, parses the logs and gets the different log levels, errors, metrics, and notifications,
these are reported to SNS topics.

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
