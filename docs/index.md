---
layout: default
---

## Overview

Marquez is an open-source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the provenance of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.

## Why Marquez?

Marquez enables highly flexible [data lineage](https://en.wikipedia.org/wiki/Data_lineage) queries accross _all datasets_, while reliably and efficiently associating (_upstream_, _downstream_) dependencies between jobs and the datasets they produce and consume.

### Key Features

* Centralized metadata management
  * Jobs
  * Datasets
* Modular
  * Data discovery
  * Data health
  * Data triggers

## Design

Marquez is a modular system and has been designed as a highly scalable, highly extensible platform-agnostic solution for metadata management. It consists of the following system components:

* **Metadata Repository**: Stores all job and dataset metadata, including a complete history of job runs and job-level statistics (i.e. total runs, average runtimes, success/failures, etc).
* **Metadata API**: RESTful API enabling a diverse set of clients to begin collecting metadata around dataset production and consumption.
* **Metadata UI**: Used for dataset discovery, connecting multiple datasets and exploring their dependency graph.

<br/>

<p align="center">
  <img src="./assets/images/design.png">
</p>

To ease adoption and enable a diverse set of data processing applications to build metadata collection as a core requirement into their design, Marquez provides language-specific SDKs that implement the **Metadata API**. As part of our initial release, we have provided support for Python.

The Metadata API is an abstraction for recording information around the production and consumption of datasets. It's a low-latency, highly-available stateless layer responsible for encapsulating both metadata persistence and aggregation of lineage information. The API allows clients to collect and/or obtain dataset information to/from the **Metadata Repository**.

Metadata needs to be collected, organized and stored in a way to allow for rich exploratory queries via the **Metadata UI**. The Metadata Repository serves as a catalog of dataset information encapsulated and cleanly abstracted away by the Metadata API.

## Data Model

The diagram below shows the metadata collected by Marquez. 

 different points i ensuring mutations are track of job and dataset mutations.

<p align="center">
  <img src="./assets/images/model.png">
</p>

**Job**: A job has a unique _name_, a _description_ and an _owner_, with one or more input / out _datasets_. It's possible for a job to have only input datasets. Marquez will create a _version_ of a job each time the job has been modified.

**Job Version:** A read-only immutable _version_ of a job, with a unique referenceable versioned artifact preserving the reproducibility of builds from source. Marquez uses versio []. In the digram above, `v1` to `v2`

**Dataset:** A dataset has a hierarchical unique _name_ and is . It has multiple versions as it gets mutated by changing the pointer to the current version. 

**Dataset Version:** A given version of a dataset. Each version can be read independently and has a unique version number for the corresponding dataset. A Version is immutable. Marquez uses dataset versions to ensure things like forward and backward compatibility and reproducibility of job runs.

## Roadmap

* Add Marquez support for [Airflow](https://airflow.apache.org)
* Add Marquez support for [Kafka](https://kafka.apache.org)
* Datasets [Icerberg](http://iceberg.incubator.apache.org) support
* Begin [modules] 


## Contributing

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [report](https://github.com/MarquezProject/marquez/issues/new) a bug
* Fix or improve documentation
* Pick up a ["good first issue"](https://github.com/MarquezProject/marquez/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [Contributor Covenant Code of Conduct](https://github.com/MarquezProject/marquez/blob/master/CODE_OF_CONDUCT.md) in all interactions with the community.

## Marquez Talks

* [Marquez: A Metadata Service for Data Abstraction, Data Lineage, and Event-based Triggers](https://www.datacouncil.ai/speaker/marquez-a-metadata-service-for-data-abstraction-data-lineage-and-event-based-triggers) by Willy Lulciuc at DataEngConf NYC '18
