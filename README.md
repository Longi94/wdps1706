# wdps1706

To run spark applications locally set the spark master to local using VM options.
`-Dspark.master=local[X]` where X is the number of threads.

Put inputs and outputs into the `spark-data` folder so they don't get accidentally committed to the repository.