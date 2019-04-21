---
layout: default
---

## Overview

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the [provenance](https://en.wikipedia.org/wiki/Provenance#Data_provenance) of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.


## Why Marquez?

Marquez enables highly flexible [data lineage](https://en.wikipedia.org/wiki/Data_lineage) queries across _all datasets_, while reliably and efficiently associating (_upstream_, _downstream_) dependencies between jobs and the datasets they produce and consume.

### Features

* Centralized **metadata management** powering:
  * [Data lineage](https://en.wikipedia.org/wiki/Data_lineage)
  * [Data governance](https://en.wikipedia.org/wiki/Data_governance)
  * Data discovery **+** exploration
* Precise and highly dimensional [data model](#data-model)
  * Jobs
  * Datasets 
* Easily collect metadata via an opinionated [Metadata API](./openapi.html)
* **Datasets** as first-class values
* **Enforcement** of _job_ and _dataset_ ownership
* Simple to operate and deploy
* RESTful API enabling sophisticated integrations with other systems:
  * [Airflow](https://airflow.apache.org)
  * [Amundsen](https://github.com/lyft/amundsenfrontendlibrary)
  * [Dagster](https://github.com/dagster-io/dagster)
* Designed to promote a **healthy** data ecosystem where teams within an organization can seamlessly _share_ datasets and _safely_ depend on one another

## Why manage and utilize metadata?

<figure align="center">
  <img src="./assets/images/healthy_data_ecosystem.png">
</figure>

## Design

Marquez is a modular system and has been designed as a highly scalable, highly extensible platform-agnostic solution for [metadata management](https://en.wikipedia.org/wiki/Metadata_management). It consists of the following system components:

* **Metadata Repository**: Stores all job and dataset metadata, including a complete history of job runs and job-level statistics (i.e. total runs, average runtimes, success/failures, etc).
* **Metadata API**: RESTful API enabling a diverse set of clients to begin collecting metadata around dataset production and consumption.
* **Metadata UI**: Used for dataset discovery, connecting multiple datasets and exploring their dependency graph.

<br/>

<figure align="center">
  <img src="./assets/images/design.png">
</figure>

To ease adoption and enable a diverse set of data processing applications to build metadata collection as a core requirement into their design, Marquez provides language-specific clients that implement the [Metadata API](./openapi.html). As part of our initial release, we have provided support for [Python](https://github.com/MarquezProject/marquez-python).

The Metadata API is an abstraction for recording information around the production and consumption of datasets. It's a low-latency, highly-available stateless layer responsible for encapsulating both metadata persistence and aggregation of lineage information. The API allows clients to collect and/or obtain dataset information to/from the **Metadata Repository**.

Metadata needs to be collected, organized and stored in a way to allow for rich exploratory queries via the [Metadata UI](https://github.com/MarquezProject/marquez-web). The Metadata Repository serves as a catalog of dataset information encapsulated and cleanly abstracted away by the Metadata API.

## Data Model

Marquez's data model emphasizes immutability and timely processing of datasets. Datasets are first-class values produced by job runs. A job run is linked to _versioned_ code, and produces one or more immutable _versioned_ outputs (derived datasets). Dataset changes are captured at different points in execution via lightweight API calls, including the success or failure of the run itself.

The diagram below shows the metadata collected and cataloged for a given job over multiple runs, and the captured time-ordered sequence of modifications applied to its input dataset.

<figure align="center">
  <img src="./assets/images/model.png">
</figure>

**Job**: A job has a unique _name_, _version_, _owner_, and an optional _description_. A job will define one or more _versioned_ inputs as dependencies, and one or more _versioned_ outputs as artifacts. Note that it's possible for a job to have only input, or only output datasets defined.

**Job Version:** A read-only immutable _version_ of a job, with a unique referenceable link to code preserving the reproducibility of builds from source. A job version associates one or more input and output datasets to a job definition. Such association catalog provenance links and provide powerful visualizations of the flow of data.

**Dataset:** A dataset has a unique _name_, _version_, _schema_, _datasource_, and _owner_. A _version_ pointer is present for each dataset and maintained by Marquez. When a dataset is modified, its _version_ pointer is updated to the current version. A datasource enables the grouping of physical datasets to their physical source.

## Roadmap

`// TODO`

## Contributing

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [report](https://github.com/MarquezProject/marquez/issues/new) a bug
* Fix or improve documentation
* Pick up a ["good first issue"](https://github.com/MarquezProject/marquez/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](https://github.com/MarquezProject/marquez/blob/master/CODE_OF_CONDUCT.md) in all interactions with the community.

## Marquez Talks

* [Marquez: A Metadata Service for Data Abstraction, Data Lineage, and Event-based Triggers](https://www.datacouncil.ai/speaker/marquez-a-metadata-service-for-data-abstraction-data-lineage-and-event-based-triggers) by Willy Lulciuc at DataEngConf NYC '18
