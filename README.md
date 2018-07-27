# Marquez

## Status

This project is under active development at [WeWork](https://www.wework.com/) in collaboration with [Stitch Fix](https://www.stitchfix.com) (and many others organizations).

## Documentation

The [Marquez design](https://drive.google.com/open?id=1zxvp-6jv4Gs7eAGFmK0fbKbYL9cbEQrRJKLk3ez4PRA) is being actively updated and is open for comments.

## Requirements

* Java 8 or higher
* Postgres Database
* Gradle 4.8 or higher

## Building

To build the entire project run:

```bash
./gradlew shadowJar
```

## Running the [Application](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/MarquezApplication.java)

```bash
$ java -jar build/libs/marquez-all.jar server config.yaml
```
Then browse to the admin interface: http://localhost:8081