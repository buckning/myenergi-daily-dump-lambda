# Description
This module contains the Lambda code to read all the historic Zappi data and back them up to DynamoDB. This can be a
long-running process as there may be a significant number of requests needed to be made to the myenergi APIs. To prevent
rate limiting by the myenergi APIs and to prevent the Lambda from timing out, this process is handled asynchronously by
chaining Lambdas. The SQS queue is to be configured with a delivery delay.

This is achieved by initially triggering the Lambda with a start date. The Lambda then calls the Zappi API to retrieve
the energy usage for the requested day, saves the returned information to DynamoDB, then sends a message to SQS to
trigger the same Lambda again but for the next day. This causes it to keep looping until the day being processed is in
the future. The Lambda breaks the loop by not pushing to SQS if the day being processed is after today.

# Create SQS Queue for priming the DB from the com.amcglynn.myenergi APIs
Queue type: fifo
Queue name: raw-data-catch-up-queue.fifo
Delivery Delay: 1 minute

# Start the process
Send a similar message as below to SQS to start the process. The body must be in the format of YYYYMMDD.
This can be done by running a test in the Lambda console and selecting `sqs-receive-message`.
```
{
  "Records": [
    {
      "messageId": "19dd0b57-b21e-4ac1-bd88-01bbb068cb78",
      "receiptHandle": "MessageReceiptHandle",
      "body": "20230314",
      "attributes": {
        "ApproximateReceiveCount": "1",
        "SentTimestamp": "1523232000000",
        "SenderId": "123456789012",
        "ApproximateFirstReceiveTimestamp": "1523232000001"
      },
      "messageAttributes": {},
      "md5OfBody": "{{{md5_of_body}}}",
      "eventSource": "aws:sqs",
      "eventSourceARN": "arn:aws:sqs:us-east-1:123456789012:MyQueue",
      "awsRegion": "us-east-1"
    }
  ]
}
```
The process will keep running every minute, writing each day at a time, and will execute until the current day is
reached. Note that this process will take longer depending on the start time. If there is a timespan of 1 year, then
the data dump will take 6 hours to complete.

The following environment variables are required:
* dbRegion
* tableName
* myEnergiHubApiKey
* myEnergiHubSerialNumber
* dataDumpSqsUrl
