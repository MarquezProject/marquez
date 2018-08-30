# Marquez

[![CircleCI](https://circleci.com/gh/MarquezProject/marquez/tree/master.svg?style=shield)](https://circleci.com/gh/MarquezProject/marquez/tree/master) [![Codecov](https://img.shields.io/codecov/c/github/MarquezProject/marquez.svg)](https://codecov.io/gh/MarquezProject/marquez) [![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez/master/LICENSE) [![Known Vulnerabilities](https://snyk.io/test/github/MarquezProject/marquez/badge.svg)](https://snyk.io/test/github/MarquezProject/marquez)

Marquez is a fundamental core service for collection, aggregation, and visualization of all metadata within a data ecosystem. It maintains the provenance of how datasets are consumed and produced, provides visibility into job runtime and frequency of dataset access, centralization of dataset lifecycle management, and much more.

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

To run Marquez, you will have to define `config.yml`. The configuration file is used to specify your database connection. Please copy and edit `config.example.yml`:

```bash
$ cp config.example.yml config.yml
```

Then run the database migration:

```bash
$ ./gradlew run --args 'db migrate config.yml'
```

**Note:** When creating your database, we recommend calling it `marquez`.

## Running the [Application](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApp.java)

```bash
$ ./gradlew run --args 'server config.yml'
```

Then browse to the admin interface: http://localhost:8081
