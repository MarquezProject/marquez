## Migrating `marquez` database manually via [`flyway`](https://flywaydb.org)

Before you can manually apply migrations to the `marquez` database, make sure you've installed `flyway`:

```bash
$ brew install flyway
``` 

**You'll also need the following details about the migration:**

|                                   | **Description**                  |
|:----------------------------------|----------------------------------|
| `[MARQUEZ_DB_HOST]`               | The db host                      |
| `[MARQUEZ_DB_PORT]`               | The db port                      |
| `[MARQUEZ_DB_USER]`               | The db user                      |
| `[MARQUEZ_DB_PASSWORD]`           | The db password                  |
| `[MARQUEZ_DB_PATH_TO_MIGRATIONS]` | The path to migrations (`*.sql`) |

To migrate the database, we'll be using the [`flyway migrate`](https://flywaydb.org/documentation/usage/commandline/migrate) command:

```bash
$ flyway migrate \
    -driver=org.postgresql.Driver \
    -url=jdbc:postgresql://[MARQUEZ_DB_HOST]:[MARQUEZ_DB_PORT]/marquez \
    -user=[MARQUEZ_DB_USER] \
    -password=[MARQUEZ_DB_PASSWORD] \
    -locations=filesystem:[MARQUEZ_DB_PATH_TO_MIGRATIONS]
```

For example, to apply the migrations defined under [`marquez/db/migration`](https://github.com/MarquezProject/marquez/tree/main/api/src/main/resources/marquez/db/migration) to the `marquez` database run:

```bash
$ flyway migrate \
    -driver=org.postgresql.Driver \
    -url=jdbc:postgresql://localhost:5432/marquez \
    -user=marquez \
    -password=*** \
    -locations=filesystem:path/to/marquez/db/migration
```