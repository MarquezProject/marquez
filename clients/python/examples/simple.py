# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

from marquez_client import Clients
from marquez_client.models import (
    DatasetType,
    DatasetId,
    JobType
)

# Construct a new MarquezClient instance we'll use to collect metadata.
# Marquez listens on 'localhost:8080' for all API calls by default.
# Use the 'MARQUEZ_URL' the environment variable to override the default
# Marquez host and port.
#
# For example:
#     $ export MARQUEZ_URL=http://localhost:5000
marquez_client = Clients.new_client()

# Create a namespace to organize related dataset and job metadata.
namespace = marquez_client.create_namespace(
    namespace_name='my-namespace',
    owner_name='me',
    description='My first namespace!'
)

# Create the source 'my-source' for the database 'mydb'.
marquez_client.create_source(
    source_type='POSTGRESQL',
    source_name='my-source',
    connection_url='jdbc:postgresql://localhost:5431/mydb',
    description='My first source!'
)

# Create the dataset 'my-dataset' and associate it with the existing source 'my-source'.
marquez_client.create_dataset(
    namespace_name='my-namespace',
    dataset_type=DatasetType.DB_TABLE,
    dataset_name='my-dataset',  # You can also use the physical name as the dataset name.
    dataset_physical_name='public.mytable',
    source_name='my-source',
    fields=[
        {'name': 'field0', 'type': 'INTEGER'},
        {'name': 'field1', 'type': 'INTEGER'},
        {'name': 'field2', 'type': 'STRING'}
    ],
    description='My first dataset!'
)

# With 'my-dataset' in Marquez, we can collect metadata for the job 'my-job'.
marquez_client.create_job(
    namespace_name='my-namespace',
    job_type=JobType.BATCH,
    job_name='my-job',
    inputs=[
      DatasetId('my-namespace', 'my-dataset')
    ],
    outputs=[],
    description='My first job!'
)

# Now, let's create a run for 'my-job' and mark the run as 'RUNNING'.
active_run = marquez_client.create_job_run(
    namespace_name='my-namespace',
    job_name='my-job',
    mark_as_running=True
)

# ID of active run to mark as 'COMPLETE'.
run_id = active_run['id']
# Mark the run for `my-job` as 'COMPLETE'.
marquez_client.mark_job_run_as_completed(run_id=run_id)
