<p align="center">
  <img src="https://github.com/MarquezProject/marquez/blob/main/docs/assets/images/marquez-logo.png" width="500px" />
</p>

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the provenance of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more. Marquez was released and open sourced by [WeWork](https://www.wework.com).

## Badges

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez/tree/main.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez/tree/main)
[![codecov](https://codecov.io/gh/MarquezProject/marquez/branch/main/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez/branch/main)
[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community)
[![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez/main/LICENSE)
[![maven](https://img.shields.io/maven-central/v/io.github.marquezproject/marquez.svg)](https://search.maven.org/search?q=g:io.github.marquezproject)
[![docker](https://img.shields.io/badge/docker-hub-blue.svg?style=flat)](https://hub.docker.com/r/marquezproject/marquez)
[![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez)

## Status

This project is under active development at [Datakin](https://twitter.com/DatakinHQ) (in collaboration with many other organizations).

## Quickstart

The Marquez [API](https://marquezproject.github.io/marquez/openapi.html) provides a simple way to collect metadata. The easiest way to get up and running is with Docker. From the base of the Marquez repository run:

```
$ docker-compose up
```

Marquez listens on port `5000` for all API calls and port `5001` for the admin interface. To verify the HTTP API server is running and listening on `localhost` browse to [http://localhost:5001](http://localhost:5001).

> **Note:** By default, the HTTP API does not require any form of authentication or authorization.

## Documentation

We invite everyone to help us improve and keep documentation up to date. Documentation is maintained in this repository and can be found under [`docs/`](https://github.com/MarquezProject/marquez/tree/main/docs).

> **Note:** To begin collecting metadata with Marquez, follow our [quickstart](https://marquezproject.github.io/marquez/quickstart.html) guide. Below you will find the steps to get up and running from source.

## Requirements

* [Java 8](https://openjdk.java.net/install)
* [PostgreSQL 9.6](https://www.postgresql.org/download)

> **Note:** To connect to your running PostgreSQL instance, you will need the standard [`psql`](https://www.postgresql.org/docs/9.6/app-psql.html) tool.

## Building

To build the entire project run:

```
$ ./gradlew shadowJar
```
The executable can be found under `build/libs/`

## Configuration

To run Marquez, you will have to define `marquez.yml`. The configuration file is passed to the application and used to specify your database connection. The configuration file creation steps are outlined below.

### Step 1: Create Database

When creating your database using [`createdb`](https://www.postgresql.org/docs/12/app-createdb.html), we recommend calling it `marquez`:

```bash
$ createdb marquez
```

### Step 2: Create `marquez.yml`

With your database created, you can now copy [`marquez.example.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.example.yml):

```
$ cp marquez.example.yml marquez.yml
```

You will then need to set the following environment variables (we recommend adding them to your `.bashrc`): `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`. The environment variables override the equivalent option in the configuration file. 

By default, Marquez uses the following ports:

* TCP port `8080` is available for the HTTP API server.
* TCP port `8081` is available for the admin interface.

> **Note:** All of the configuration settings in `marquez.yml` can be specified either in the configuration file or in an environment variable.

## Running the [Application](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/MarquezApp.java)

```bash
$ ./gradlew runShadow
```

Then browse to the admin interface: http://localhost:8081

## Related Projects

* [`marquez-web`](https://github.com/MarquezProject/marquez-web): Web UI used to view metadata collected and cataloged by Marquez.
* [`marquez-airflow`](https://github.com/MarquezProject/marquez-airflow): Airflow support for Marquez.
* [`marquez-java`](https://github.com/MarquezProject/marquez-java): Java client for Marquez.
* [`marquez-python`](https://github.com/MarquezProject/marquez-python): Python client for Marquez.

## Getting Involved

* Website: https://marquezproject.ai
* Source: https://github.com/MarquezProject/marquez
* Chat: https://gitter.im/marquez-project/community
* Twitter: [@MarquezProject](https://twitter.com/MarquezProject)

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md) for more details about how to contribute.
