# Marquez

[![status](https://img.shields.io/badge/status-WIP-yellow.svg)](#status) [![license](https://img.shields.io/badge/license-Apache_2.0-blue.svg)](https://raw.githubusercontent.com/MarquezProject/marquez/master/LICENSE)

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
$ ./gradlew build
```
The executable can be found under `build/libs/`

## Configuration

To run Marquez, you will have to define `config.yaml`. The configuration file is used to specify your database connection. Please copy and edit `config.example.yaml`:

```bash
$ cp config.example.yaml config.yaml
```

Then run the database migration:

```bash
$ java -jar build/libs/marquez.jar db migrate config.yaml
```

**Note:** When creating your database, we recommend calling it `marquez`.

## Running the [Application](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApplication.java)

```bash
$ java -jar build/libs/marquez.jar server config.yaml
```

Then browse to the admin interface: http://localhost:8081
