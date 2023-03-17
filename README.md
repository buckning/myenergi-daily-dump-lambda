myenergi stores all energy usage of your solar, input and output to the grid, consumption. There are some APIs to
retrieve this information but there could be additional functions that would be useful.

The purpose of this project is to extract this information, back up the raw data in another DB.

This project has two main functions, each of which has its own module.
1. Gather all historic data (zappi-history-dump-lambda)
2. Scheduled retrieval of recent data (zappi-nightly-data-dump)
