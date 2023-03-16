myenergi stores all energy usage of your solar, input and output to the grid, consumption. There are some APIs to
retrieve this information but there could be additional functions that would be useful.

The purpose of this project is to extract this information, back up the raw data in another DB and have APIs to retrieve
the data for different use cases.

# Initial data gathering
The first step of this project is to extract all of the data from com.amcglynn.myenergi and store it in a DB. This is a long-running
process so it is handled asynchronously by chaining lambdas together through SQS. The first lambda works with one date,
invokes the com.amcglynn.myenergi API to retrieve the data, stores it in the DB and then pushes an SQS event for the next day. The
breakout of this chain when the date being processed is today, it does not call the com.amcglynn.myenergi API or push an event to SQS.
This is initiated by the StartHandler, which simply reads the start date and pushes the event to SQS.

There may be cases where data is missing from the dataset. If this is the case, a reattempt is scheduled in SQS to retry
for up to 7 days.

# Scheduled data gathering
Each day a scheduled cron job is triggered at 6AM by cloudwatch events to pull the data for the previous day.
There may be cases where data is missing from the dataset. If this is the case, a reattempt is scheduled in SQS to retry
for up to 7 days.
