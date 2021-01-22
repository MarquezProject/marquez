# Marquez Spark Agent
The Marquez Spark Agent uses jvm instrumentation to emit OpenLineage metadata to Marquez. 


## Getting started

### Dataproc
Dataproc requires two things: a uri to the marquez java agent jar in the `files` parameter and 
an additional spark property. Dataproc will copy the agent jar to the current working directory of the
executor and the `-javaagent` parameter will load it on execution.

```python
from uuid import uuid4
...

jar = 'marquez-spark-LATEST.jar'
files = [f"gs://bq-airflow-spark/{jar}"]
marquez_path = 'https://marquez.example.org:5000'
run_id = uuid4()
job_name = 'submit_job'
properties = {
  'spark.driver.extraJavaOptions':
  f"-javaagent:{jar}={marquez_path}/api/v1/namespaces/foo/job/{job_name}/runs/{run_id}"
}

t1 = DataProcPySparkOperator(
    task_id=job_name,
    gcp_conn_id='google_cloud_default',
    project_id='your-gcp-project-id',
    main='gs://gcp-bucket/word-count.py',
    job_name='WordCount',
    dataproc_pyspark_properties=properties,
    cluster_name='your-dataproc-cluster',
    files=files,
    region='us-west1',
    dag=dag)
```

### Spark Submit
```python
from uuid import uuid4
import os
...

spark_home = os.getenv('SPARK_HOME')
marquez_path = 'http://localhost:5000'
run_id = uuid4()
jar_path = 'marquez-spark-LATEST.jar'
job_name = 'submit_job'
properties = {
'spark.driver.extraJavaOptions':
f"-javaagent:{jar_path}={marquez_path}/api/v1/namespaces/foo/job/{job_name}/runs/{run_id}"
}

t1 = SparkSubmitOperator(
    task_id=job_name,
    conn_id='spark_local',
    master='local',
    conf=properties,
    application=f"{spark_home}/work-dir/word-count.jar",
    dag=dag
)

```

## Arguments
The java agent accepts an argument in the form of a uri. It includes the location of Marquez, the 
namespace name, the job name, and a unique run id. This run id will be emitted as a parent run 
facet.
```
{marquez_home}/api/v1/namespaces/{namespace}/job/{job_name}/runs/{run_uuid}"

```
For example:
```
https://marquez.example.com:5000/api/v1/namespaces/foo/job/spark.submit_job/runs/a95858ad-f9b5-46d7-8f1c-ca9f58f68978"
```

# Build

## Testing
To run the tests, run:
```sh
./gradlew test
```

## Build spark agent jar
```sh
./gradlew shadowJar
```