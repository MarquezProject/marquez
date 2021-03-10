# Lifecycle

Marquez captures the runs of a job, and changes as they happen.

Run states:
```
NEW -(start)-> RUNNING -(complete)-> COMPLETED
                       -(abort)-> ABORTED
                       -(fail)-> FAILED
```

## Static inspection of job lineage
When the job lineage can be inspected from its static definition the metadata is captured as follows:

### When the run of the job starts
 - PUT /namespaces/{name} (for every namespace referred to below)
 Before referring to a namespace we must ensure its metadata has been updated
 - PUT /sources/{name} (for every source referred to by a dataset below)
 Before referring to a datasource we must ensure its metadata has been updated
 - PUT /namespaces/{namespace}/datasets/{name} (for every dataset referred to as an input or output of the job below)
  Before referring to an input or output dataset we must ensure its metadata has been updated. This is the dataset as it is inspected before the job starts.
 - PUT /namespaces/{namespace}/jobs/{name}
 Now that we have updated all the dependencies of the job we can update the job itself
 - POST /namespaces/{namespace}/jobs/{name}/runs
 We create a run for this job
 - POST /namespaces/{namespace}/jobs/{name}/runs/{id}/start
 We start the run

### When the run of the job ends
 - PUT /namespaces/{namespace}/datasets/{name} (for every dataset the job wrote to during that run)
  We update the definition of each dataset this job wrote to during the run. The dataset updates *must* refer to the runId. For example the schema might have changed
- POST /namespaces/{namespace}/jobs/{name}/runs/{id}/complete
 We mark the run as succeful (similarly 'fail' for a failed job)

## Dynamic inspection of job lineage
If the job can not be introspected statically, we will have to capture the information as the job is running. Then all the updates will be sent when the job is actually completing. (both steps above are executed when the job finishes)

### When the run of the job starts
 - PUT /namespaces/{name} (for every namespace referred to below)
 Before referring to a namespace we must ensure its metadata has been updated
 - PUT /namespaces/{namespace}/jobs/{name}
 We can update the job itself, but we don't know the inputs and outputs yet
 - POST /namespaces/{namespace}/jobs/{name}/runs
 We create a run for this job
 - POST /namespaces/{namespace}/jobs/{name}/runs/{id}/start
 We start the run

### When the run of the job ends
 - PUT /sources/{name} (for every source referred to by a dataset below)
 Before referring to a datasource we must ensure its metadata has been updated
 - PUT /namespaces/{namespace}/datasets/{name} (for every input dataset)
  Before referring to an input, we need to make sure its metadata has been updated. (We will not refer to the runid in those calls)
 - PUT /namespaces/{namespace}/datasets/{name} (for every dataset the job wrote to during that run)
  We update the definition of each dataset this job wrote to during the run. The dataset updates *must* refer to the runId that produced. For example the schema might have changed
 - PUT /namespaces/{namespace}/jobs/{name}
 Now that we have updated all the dependencies of the job we can update the job itself, we *must* refer to the runId that this is for.
- POST /namespaces/{namespace}/jobs/{name}/runs/{id}/complete
 We mark the run as successful (similarly 'fail' for a failed job)

## Unified case
### When the run of the job starts

#### extract info from the job
 - PUT /namespaces/{name} (for every namespace referred to below)
 Before referring to a namespace we must ensure its metadata has been updated
 - if we could extract inputs and outputs
    - PUT /sources/{name} (for every source referred to by a dataset below)
    Before referring to a datasource we must ensure its metadata has been updated
    - PUT /namespaces/{namespace}/datasets/{name} (for every dataset referred to as an input or ouput of the job below)
    Only if the inputs and outputs were extracted by the extractor.
    Before referring to an input or output dataset we must ensure its metadata has been updated. This is the dataset as it is inspected before the job starts.
 - PUT /namespaces/{namespace}/jobs/{name}
 Now that we have updated all the dependencies of the job we can update the job itself
 - POST /namespaces/{namespace}/jobs/{name}/runs
 We create a run for this job
 - POST /namespaces/{namespace}/jobs/{name}/runs/{id}/start
 We start the run

### When the run of the job ends
 - PUT /sources/{name} (for every source referred to by a dataset below)
 Before referring to a datasource we must ensure its metadata has been updated
 - PUT /namespaces/{namespace}/datasets/{name} (for every input dataset)
  Before referring to an input, we need to make sure its metadata has been updated. (We will not refer to the runId in those calls)
 - PUT /namespaces/{namespace}/datasets/{name} (for every dataset the job wrote to during that run)
   - if the job was successful:
      We update the definition of each dataset this job wrote to during the run. The dataset updates **must refer to the runId** that produced. For example the schema might have changed
   - if the job failed: we update the output job but **we don't refer to the runId** because the run did not modify the dataset
 - PUT /namespaces/{namespace}/jobs/{name}
 Now that we have updated all the dependencies of the job we can update the job itself, we **must refer to the runId** that this is for.
- POST /namespaces/{namespace}/jobs/{name}/runs/{id}/complete
 We mark the run as successful (similarly 'fail' for a failed job)
