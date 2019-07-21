# Marquez

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez/tree/master) [![codecov](https://codecov.io/gh/MarquezProject/marquez/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez/branch/master) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/marquez-project/community) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez/master/LICENSE) [![docker](https://img.shields.io/badge/docker-hub-blue.svg?style=flat)](https://hub.docker.com/r/marquezproject/marquez) [![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez)

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the provenance of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.

## Status

This project is under active development at [The We Company](https://www.we.co) (in collaboration with many others organizations).

## Documentation

We invite everyone to help us improve and keep documentation up to date. Documentation is maintained in this repository and can be found under [`docs/`](https://github.com/MarquezProject/marquez/tree/master/docs).

> **Note:** To begin collecting metadata with Marquez, follow our [quickstart](https://marquezproject.github.io/marquez/quickstart.html) guide. Below you will find the steps to get up and running from source.

## Requirements

* [Java 8](https://openjdk.java.net/install)+
* [PostgreSQL 9.6](https://www.postgresql.org/download)

> **Note:** To connect to your running PostgreSQL instance, you will need the standard [`psql`](https://www.postgresql.org/docs/9.6/app-psql.html) tool.

## Building

To build the entire project run:

```
$ ./gradlew shadowJar
```
The executable can be found under `build/libs/`

## Configuration

To run Marquez, you will have to define `config.yml`. The configuration file is passed to the application and used to specify your database connection. The configuration file creation steps are outlined below.

### Step 1: Create Database

When creating your database using [`psql`](https://www.postgresql.org/docs/9.6/app-psql.html), we recommend calling it `marquez`:

```bash
$ psql -c 'CREATE DATABASE marquez;'
```

### Step 2: Create `config.yml`

With your database created, you can now copy [`config.example.yml`](https://github.com/MarquezProject/marquez/blob/master/config.example.yml):

```
$ cp config.example.yml config.yml
```

You will then need to set the following environment variables (we recommend adding them to your `.bashrc`): `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`. The environment variables override the equivalent option in the configuration file. 

By default, Marquez uses the following ports:

* TCP port `8080` is available for the HTTP API server.
* TCP port `8081` is available for the admin interface.

> **Note:** All of the configuration settings in `config.yml` can be specified either in the configuration file or in an environment variable.

## Running the [Application](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApp.java)

```bash
$ ./gradlew run --args 'server config.yml'
```

Then browse to the admin interface: http://localhost:8081

## Getting Involved

* Website: https://marquezproject.github.io/marquez
* Mailing Lists:
  * [marquez-user@googlegroups.com](https://groups.google.com/group/marquez-user) (_user support and questions_)
  * [marquez-dev@googlegroups.com](https://groups.google.com/group/marquez-dev) (_development discussions_)
* Chat: https://gitter.im/marquez-project/community
* Twitter: [@MarquezProject](https://twitter.com/MarquezProject)

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez/blob/master/CONTRIBUTING.md) for more details about how to contribute.
