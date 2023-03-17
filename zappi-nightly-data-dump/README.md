# Description
A cron job is configured to run every day at 6AM (`0 6 * * ? *`).
This requires EventBridge(CloudWatch Events) to be configured on a recurring schedule with the above cron pattern.

Once executed, the Lambda reads the information from myenergi for the day before yesterday and then saves it to DynamoDB.
From experience, Zappi can take quite some time to push all the readings for a day. Giving it 18 hours extra to gather
the data should cater for most of these delays.

The following environment variables are required:
* dbRegion
* tableName
* myEnergiHubApiKey
* myEnergiHubSerialNumber