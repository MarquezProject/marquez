# Marquez

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez/tree/master) [![Codecov](https://img.shields.io/codecov/c/github/MarquezProject/marquez.svg)](https://codecov.io/gh/MarquezProject/marquez) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez/master/LICENSE) [![docker](https://img.shields.io/badge/docker-hub-blue.svg?style=flat)](https://hub.docker.com/r/projectmarquez/marquez/) [![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez)

Marquez is an open source **metadata service** for the **collection**, **aggregation**, and **visualization** of a data ecosystem's metadata. It maintains the provenance of how datasets are consumed and produced, provides global visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.

## Status

This project is under active development at [WeWork](https://www.wework.com/) and [Stitch Fix](https://www.stitchfix.com) (in collaboration with many others organizations).

## Documentation

The [Marquez design](https://drive.google.com/open?id=1zxvp-6jv4Gs7eAGFmK0fbKbYL9cbEQrRJKLk3ez4PRA) is being actively updated and is open for comments.

## Requirements

* Java 8 or above
* PostgreSQL database
* Gradle 4.9 or above

## Building

To build the entire project run:

```bash
$ ./gradlew shadowJar
```
The executable can be found under `build/libs/`

## Configuration

To run Marquez, you will have to define `config.yml` (see [reference](https://www.dropwizard.io/1.3.5/docs/manual/configuration.html)). The configuration file is passed to the application and used to specify your database connection. When creating your database, we recommend calling it `marquez`. Please copy `config.example.yml`:

```bash
$ cp config.example.yml config.yml
```

You will then need to edit it and specify your database information.

**Note:** As an optional step, you can manually run the database migration with:

```bash
$ ./gradlew run --args 'db migrate config.yml'
```

**Tip:** You can access environment variables from `config.yml` with [`${VAR_NAME}`](https://www.dropwizard.io/1.3.5/docs/manual/core.html#environment-variables).

## Running the [Application](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApp.java)

```bash
$ ./gradlew run --args 'server config.yml'
```

Then browse to the admin interface: http://localhost:8081

## Getting involved

* Website: https://marquezproject.github.io/marquez
* Mailing Lists:
  * [marquez-user@googlegroups.com](https://groups.google.com/group/marquez-user) (_user support and questions_)
  * [marquez-dev@googlegroups.com](https://groups.google.com/group/marquez-dev) (_development discussions_)
* Twitter: [@MarquezProject](https://twitter.com/MarquezProject)

## Contributing

 See [CONTRIBUTING.md](./CONTRIBUTING.md) for more details about how to contribute.
