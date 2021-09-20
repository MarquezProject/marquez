from marquez_client import Clients
from marquez_client.models import (
    DatasetType,
    DatasetId,
    JobType,
    JobId,
    RunState
)

# Construct a new MarquezClient instance we'll use to collect metadata.
# Marquez listens on 'localhost:8080' for all API calls by default.
# Use the 'MARQUEZ_URL' the environment variable to override the default
# Marquez host and port.
#
# For example:
#     $ export MARQUEZ_URL=http://localhost:5000
marquez_client = Clients.new_client()

# Before we can begin collecting metadata, we must first create a namespace.
# A namespace helps you organize related dataset and job metadata. Note that datasets
# and jobs are unique within a namespace, but not across namespaces. In this example,
# we'll use the namespace 'my-namespace'.
marquez_client.create_namespace(
    namespace_name="my-namespace",
    owner_name="me",
    description="My first namespace."
)

# Each dataset must be associated with a source. A source is the physical location of a
# dataset, such as a table in a database, or a file on cloud storage. A source enables the
# logical grouping and mapping of physical datasets to their physical source. Below, let's
# create the source 'my-source' for the database 'mydb'.
marquez_client.create_source(
    source_type="POSTGRESQL",
    source_name="my-source",
    connection_url="jdbc:postgresql://localhost:5431/mydb",
    description="My first source."
)

# Next, we need to create the dataset 'my-dataset' and associate it with the existing source 'my-source'.
# In Marquez, datasets have both a logical and physical name. The logical name is how your dataset is
# known to Marquez, while the physical name is how your dataset is known to your source. In this example,
# we refer to 'my-dataset' as the logical name and 'public.mytable' (format: 'schema.table') as the physical name.
dataset = marquez_client.create_dataset(
    namespace_name="my-namespace",
    dataset_type=DatasetType.DB_TABLE,
    dataset_name="my-dataset",
    dataset_physical_name="public.mytable",
    source_name="my-source",
    fields=[
        {'name': 'field0', 'type': 'INTEGER'},
        {'name': 'field1', 'type': 'INTEGER'},
        {'name': 'field2', 'type': 'STRING'}
    ],
    description="My first dataset."
)

# ...
dataset_version = marquez_client.get_dataset_version(
    namespace_name="my-namespace",
    dataset_name="my-dataset",
    version=dataset["currentVersion"]
)
println(dataset_version)
