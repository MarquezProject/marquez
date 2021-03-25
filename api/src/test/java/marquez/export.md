To use neptune:
1. Create a neptune instance
2. Enable bulk loading: https://docs.aws.amazon.com/neptune/latest/userguide/bulk-load-tutorial-IAM.html
3. Run export script to generate edges and vertices
4. Upload them to an S3 bucket
5. Run the following commands, replace: <ID FROM PROCESS ABOVE> with the id generated from the first command.

```sh
curl -v -k -X POST \
    -H 'Content-Type: application/json' \
    https://localhost:8182/loader -d '
    {
      "source" : "s3://datakin-neptune/v",
        "format" : "csv",
        "iamRoleArn" : "arn:aws:iam::860419233076:role/NeptuneLoadFromS3",
        "region" : "us-west-2",
        "failOnError" : "FALSE",
        "parallelism" : "MEDIUM",
        "updateSingleCardinalityProperties" : "FALSE",
        "queueRequest" : "TRUE",
        "dependencies" : []
    }'
```
```sh
curl -v -k -X POST \
    -H 'Content-Type: application/json' \
    https://localhost:8182/loader -d '
    {
      "source" : "s3://datakin-neptune/e",
        "format" : "csv",
        "iamRoleArn" : "arn:aws:iam::860419233076:role/NeptuneLoadFromS3",
        "region" : "us-west-2",
        "failOnError" : "FALSE",
        "parallelism" : "MEDIUM",
        "updateSingleCardinalityProperties" : "FALSE",
        "queueRequest" : "TRUE",
        "dependencies" : [<ID FROM PROCESS ABOVE>]
    }'
```

Check status for any errors for both jobs:
```sh
curl -k -G 'https://localhost:8182/loader/<ID>'
```

6. Verify gremlin works
```sh
curl -k -X POST -d '{"gremlin":"g.V().limit(1)"}' https://localhost:8182/gremlin
```

Queries:
Similar jobs based on input
```gremlin
g.V().has('job','JobName','example.delivery_times_7_days').as('d')
.out('inputs')
.in('inputs')
.where(neq('d'))
.dedup()
.values('JobName')
```

To delete it all and restart:
```sh
curl -k -X POST -d '{"gremlin":"g.V().drop().iterate()"}'  https://localhost:8182/gremlin
```