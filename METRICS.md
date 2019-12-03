# Metrics

| Metric                           | Type    | Tags                                                       | Description                         |
|----------------------------------|---------|------------------------------------------------------------|-------------------------------------|
| `marquez_namespace_total`        | _count_ |                                                            | Total number of namespaces.         |
| `marquez_source_total`           | _count_ | `source_type`                                              | Total number of sources.            |
| `marquez_dataset_total`          | _count_ | `namespace_name`, <br> `dataset_type`                      | Total number of datasets.           |
| `marquez_dataset_versions_total` | _count_ | `namespace_name`, <br> `dataset_type`, <br> `dataset_name` | Total number of dataset versions.   |
| `marquez_job_total`              | _count_ | `namespace_name`, <br> `job_type`                          | Total number of jobs.               |
| `marquez_job_versions_total`     | _count_ | `namespace_name`, <br> `job_type`, <br> `job_name`         | Total number of job versions.       |
| `marquez_job_runs_active`        | _gauge_ |                                                            | Total number of active job runs.    |
| `marquez_job_runs_completed`     | _gauge_ |                                                            | Total number of completed job runs. |