---
layout: index
---

## Overview

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the [provenance](https://en.wikipedia.org/wiki/Provenance#Data_provenance) of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more. Marquez was released and open sourced by [WeWork](https://www.wework.com).

#### FEATURES

* Centralized [metadata management](https://en.wikipedia.org/wiki/Metadata_management) powering:
  * Data lineage
  * [Data governance](https://en.wikipedia.org/wiki/Data_governance)
  * Data health
  * Data discovery **+** exploration
* Precise and highly dimensional [data model](#data-model)
  * Jobs
  * Datasets 
* Easily collect metadata via an opinionated [Metadata API](./openapi.html)
* **Datasets** as first-class values
* **Enforcement** of _job_ and _dataset_ ownership
* Simple operation and design with minimal dependencies
* RESTful API enabling sophisticated integrations with other systems:
  * [Airflow](https://airflow.apache.org)
  * [Amundsen](https://github.com/lyft/amundsenfrontendlibrary)
  * [Dagster](https://github.com/dagster-io/dagster)
* Designed to promote a **healthy** data ecosystem where teams within an organization can seamlessly _share_ and _safely_ depend on one another's datasets with confidence

## Why Marquez?

Marquez enables highly flexible [data lineage](https://en.wikipedia.org/wiki/Data_lineage) queries across _all datasets_, while reliably and efficiently associating (_upstream_, _downstream_) dependencies between jobs and the datasets they produce and consume.

<figure align="center">
  <img src="./assets/images/lineage.png">
</figure>

## Why manage and utilize metadata?

<figure align="center">
  <img src="./assets/images/ecosystem.png">
</figure>

## Design

Marquez is a modular system and has been designed as a highly scalable, highly extensible platform-agnostic solution for metadata management. It consists of the following system components:

* **Metadata Repository**: Stores all job and dataset metadata, including a complete history of job runs and job-level statistics (i.e. total runs, average runtimes, success/failures, etc).
* **Metadata API**: RESTful API enabling a diverse set of clients to begin collecting metadata around dataset production and consumption.
* **Metadata UI**: Used for dataset discovery, connecting multiple datasets and exploring their dependency graph.

<br/>

<figure align="center">
  <img src="./assets/images/design.png">
</figure>

To ease adoption and enable a diverse set of data processing applications to build metadata collection as a core requirement into their design, Marquez provides language-specific clients that implement the [Metadata API](./openapi.html). As part of our initial release, we have provided support for [Java](https://github.com/MarquezProject/marquez-java) and [Python](https://github.com/MarquezProject/marquez-python).

The Metadata API is an abstraction for recording information around the production and consumption of datasets. It's a low-latency, highly-available stateless layer responsible for encapsulating both metadata persistence and aggregation of lineage information. The API allows clients to collect and/or obtain dataset information to/from the [Metadata Repository](https://www.lucidchart.com/documents/view/f918ce01-9eb4-4900-b266-49935da271b8/0).

Metadata needs to be collected, organized, and stored in a way to allow for rich exploratory queries via the [Metadata UI](https://github.com/MarquezProject/marquez-web). The Metadata Repository serves as a catalog of dataset information encapsulated and cleanly abstracted away by the Metadata API.

## Data Model

Marquez's data model emphasizes immutability and timely processing of datasets. Datasets are first-class values produced by job runs. A job run is linked to _versioned_ code, and produces one or more immutable _versioned_ outputs. Dataset changes are recorded at different points in job execution via lightweight API calls, including the success or failure of the run itself.

The diagram below shows the metadata collected and cataloged for a given job over multiple runs, and the time-ordered sequence of changes applied to its input dataset.

<figure align="center">
  <img src="./assets/images/model.png">
</figure>

**Job**: A job has an `owner`, unique `name`, `version`, and optional `description`. A job will define one or more _versioned_ inputs as dependencies, and one or more _versioned_ outputs as artifacts. Note that it's possible for a job to have only input, or only output datasets defined.

**Job Version:** A read-only _immutable_ `version` of a job, with a unique referenceable `link` to code preserving the reproducibility of builds from source. A job version associates one or more input and output datasets to a job definition (important for lineage information as data moves through various jobs over time). Such associations catalog provenance links and provide powerful visualizations of the flow of data.

**Dataset:** A dataset has an `owner`, unique `name`, `schema`, `version`, and optional `description`. A dataset is contained within a datasource. A `datasource` enables the grouping of physical datasets to their physical source. A version `pointer` into the historical set of changes is present for each dataset and maintained by Marquez. When a dataset change is committed back to Marquez, a distinct version ID is generated, stored, then set to `current` with the pointer updated internally.

**Dataset Version:** A read-only _immutable_ `version` of a dataset. Each version can be read independently and has a unique ID mapped to a dataset change preserving its state at some given point in time. The _latest_ version ID is updated only when a change to the dataset has been recorded. To compute a distinct version ID, Marquez applies a versioning function to a set of properties corresponding to the datasets underlying datasource.

## Contributing

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [report](https://github.com/MarquezProject/marquez/issues/new) a bug
* Fix or improve documentation
* Pick up a ["good first issue"](https://github.com/MarquezProject/marquez/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](https://github.com/MarquezProject/marquez/blob/main/CODE_OF_CONDUCT.md) in all interactions with the community.

If you’re interested in using or learning more about Marquez, reach out to us on [gitter](https://gitter.im/marquez-project/community) and follow [@MarquezProject](https://twitter.com/MarquezProject) for updates.

## Marquez Talks

* [Solving Data Lineage Tracking And Data Discovery At WeWork](https://www.dataengineeringpodcast.com/marquez-data-lineage-episode-111) on [The Data Engineering Podcast](https://www.dataengineeringpodcast.com/)
* [Data Lineage with Apache Airflow using Marquez](https://www.youtube.com/watch?v=BIVUXruv5io) by Willy Lulciuc at CRUNCH '19
* [Marquez: An Open Source Metadata Service for ML Platforms](https://www.slideshare.net/WillyLulciuc/marquez-an-open-source-metadata-service-for-ml-platforms) by Willy Lulciuc, Shawn Shah at AI NEXTCon SF '19
* [Marquez: A Metadata Service for Data Abstraction, Data Lineage, and Event-based Triggers](https://www.datacouncil.ai/speaker/marquez-a-metadata-service-for-data-abstraction-data-lineage-and-event-based-triggers) by Willy Lulciuc at DataEngConf NYC '18
