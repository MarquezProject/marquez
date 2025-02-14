# FAQ

### How do I view migrations applied to the `marquez` database schema?

We use [`flyway`](https://flywaydb.org) to apply migrations to the `marquez` database; migrations can be found under [`marquez/db/migration/`](https://github.com/MarquezProject/marquez/tree/main/api/src/main/resources/marquez/db/migration).

### How do I view the _current_ `marquez` database schema?

```bash
pg_dump -d marquez --schema-only > schema.sql
```

### How do I manually apply migrations to the `marquez` database using [`flyway`](https://flywaydb.org)?

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
flyway migrate \
    -driver=org.postgresql.Driver \
    -url=jdbc:postgresql://[MARQUEZ_DB_HOST]:[MARQUEZ_DB_PORT]/marquez \
    -user=[MARQUEZ_DB_USER] \
    -password=[MARQUEZ_DB_PASSWORD] \
    -locations=filesystem:[MARQUEZ_DB_PATH_TO_MIGRATIONS]
```

For example, to apply the migrations defined under [`marquez/db/migration/`](https://github.com/MarquezProject/marquez/tree/main/api/src/main/resources/marquez/db/migration) to the `marquez` database run:

```bash
flyway migrate \
    -driver=org.postgresql.Driver \
    -url=jdbc:postgresql://localhost:5432/marquez \
    -user=marquez \
    -password=*** \
    -locations=filesystem:path/to/marquez/db/migration
```

### How do I configure a retention policy for metadata?

By default, Marquez does not apply a retention policy on collected metadata. However, you can adjust the maximum retention days for metadata in Marquez. This allows you to better manage your storage space and comply with your organizational retention policies. Below, you'll find examples of how to change retention days in `YAML` and via the [CLI](https://github.com/MarquezProject/marquez/tree/main/api/src/main/java/marquez/cli):

**`YAML`**

To adjust the retention period, add a **`dbRetention`** section in your [`marquez.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.example.yml):

```yaml
# Adjusts retention policy
dbRetention:
  # Apply retention policy at a frequency of every '15' minutes
  frequencyMins: 15
  # Maximum number of rows deleted per batch
  numberOfRowsPerBatch: 1000
  # Maximum retention days
  retentionDays: 7
```

**`CLI`**

To run a  _one-off_ _ad-hoc_ retention policy on your metadata, use the [`db-retention`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/DbRetentionCommand.java) command:

```bash
java -jar marquez-api.jar db-retention \
  --number-of-rows-per-batch 1000 \
  --retention-days 7 \
  marquez.yml
```

Use the `--dry-run` flag to output an estimate of metadata deleted by the retention policy:

```bash
java -jar marquez-api.jar db-retention \
  --number-of-rows-per-batch 1000 \
  --retention-days 7 \
  --dry-run \
  marquez.yml
```

### How do I filter namespaces using regex patterns in Marquez?

In Marquez, you may find the need to exclude certain namespaces from being fetched. This can be particularly useful when you want to filter out namespaces based on specific patterns. To achieve this, you can use the `exclude` feature in the `marquez.yml` configuration file.

Here's how you can set it up:

```yaml
exclude:
  namespaces:
    onRead:
      enabled: boolean 
      pattern: "<pattern0>|<pattern1>|..."
    onWrite:
      enabled: boolean  
      pattern: "<pattern0>|<pattern1>|..."
```
In the above configuration:

- `onRead.enabled: true` indicates that the exclusion should happen when reading namespaces.
- `pattern` is a string of regular expressions. Any namespace matching any of these patterns will be excluded.
Replace <pattern0> and <pattern1> with the actual regex patterns you want to use for filtering namespaces. You can add as many patterns as you need.

This feature provides a flexible way to manage the namespaces that are read by Marquez, allowing you to fine-tune the list of namespaces that are presented in the UI.

For the moment, the exclusion only works for filtering namespaces when Marquez is querying them from its database (onRead), but we plan to expand the same logic to databases and jobs not only on read, but also on write to prevent any unwanted data to be sent to the backend.