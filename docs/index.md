---
layout: default
---

## Marquez

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the provenance of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.

![](./assets/images/design.png)


## Data Model

![](./assets/images/model.png)

**Job:** It has a unique name and is owned by a team that can deploy versions of it in production. It has a current version.

**Job Version:** it is an immutable version of a job, pointing to a unique versioned artifact and the metadata for a reproducible build from source.

**Dataset:** it has a hierarchical unique name in its database. It has multiple versions as it gets mutated by changing the pointer to the current version.

**Dataset Version:** A given version of a dataset. Each version can be read independently and has a unique version number for the corresponding dataset. A Version is immutable.

## Roadmap

## Contributing

We're excited you're interested in contributing to Marquez! We'd love your help, and there are plenty of ways to contribute:

* Fix or [**report**](https://github.com/MarquezProject/marquez/issues/new) a bug
* Fix or improve documentation
* Pick up a [**"good first issue"**](https://github.com/MarquezProject/marquez/labels/good%20first%20issue), then send a pull request our way

We feel that a welcoming community is important and we ask that you follow the [**Contributor Covenant Code of Conduct**](https://github.com/MarquezProject/marquez/blob/master/CODE_OF_CONDUCT.md) in all interactions with the community.

## Marquez Talks

* [**Marquez: A Metadata Service for Data Abstraction, Data Lineage, and Event-based Triggers**](https://www.datacouncil.ai/speaker/marquez-a-metadata-service-for-data-abstraction-data-lineage-and-event-based-triggers) by Willy Lulciuc at DataEngConf NYC '18
