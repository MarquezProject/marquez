# Marquez

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez/tree/master) [![codecov](https://codecov.io/gh/MarquezProject/marquez/branch/master/graph/badge.svg)](https://codecov.io/gh/MarquezProject/marquez/branch/master) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez/master/LICENSE) [![docker](https://img.shields.io/badge/docker-hub-blue.svg?style=flat)](https://hub.docker.com/r/marquezproject/marquez) [![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez)

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the provenance of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.

## Status

This project is under active development at [WeWork](https://www.wework.com) and [Stitch Fix](https://www.stitchfix.com) (in collaboration with many others organizations).

## Documentation

The [Marquez design](https://drive.google.com/open?id=1zxvp-6jv4Gs7eAGFmK0fbKbYL9cbEQrRJKLk3ez4PRA) is being actively updated and is open for comments.

## Requirements

* [Java 8](https://openjdk.java.net/install)+
* [PostgreSQL 9.6](https://www.postgresql.org/download)
* [Gradle 4.10](https://gradle.org/releases)+ (_optional_)

## Building

To build the entire project run:

```bash
$ ./gradlew shadowJar
```
The executable can be found under `build/libs/`

## Configuration

To run Marquez, you will have to define `config.yml`. The configuration file is passed to the application and used to specify your database connection. When creating your database, we recommend calling it `marquez`. Please copy [`config.example.yml`](https://github.com/MarquezProject/marquez/blob/master/config.example.yml):

```bash
$ cp config.example.yml config.yml
```

You will then need to set the following environment variables (we recommend adding them to your `.bashrc`): `POSTGRES_DB`, `POSTGRES_USER`, and `POSTGRES_PASSWORD`.

**Note:** All environment variables in `config.yml` are accessed with [`${VAR_NAME}`](https://www.dropwizard.io/1.3.5/docs/manual/core.html#environment-variables).

## Running the [Application](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApp.java)

```bash
$ ./gradlew run --args 'server config.yml'
```

Then browse to the admin interface: http://localhost:8081

## Running with [Docker](./Dockerfile) 

```
$ docker-compose up
```

## Getting involved

* Website: https://marquezproject.github.io/marquez
* Mailing Lists:
  * [marquez-user@googlegroups.com](https://groups.google.com/group/marquez-user) (_user support and questions_)
  * [marquez-dev@googlegroups.com](https://groups.google.com/group/marquez-dev) (_development discussions_)
* Chat: https://gitter.im/marquez-project/community
* Twitter: [@MarquezProject](https://twitter.com/MarquezProject)

## Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez/blob/master/CONTRIBUTING.md) for more details about how to contribute.
