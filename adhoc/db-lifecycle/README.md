# Running DB Retention Lifecycle Policy Ad-Hoc

By default, Marquez does not apply a retention policy on collected metadata. However, you can adjust the maximum retention days for metadata in Marquez. This allows you to better manage your storage space and comply with your organizational retention policies. Below, you'll find how to change retention days in via the [CLI](https://github.com/MarquezProject/marquez/tree/main/api/src/main/java/marquez/cli) and run it ad-hoc.


## Setup
### Environment Variables
These env vars are necessary for logging into the target Postgres database:
```
export POSTGRES_HOST=<Aurora RDS cluster's endpoint URL>
export POSTGRES_PORT=5432
export POSTGRES_USERNAME=<Username>
export POSTGRES_PASSWORD=<Password>
```

### Build Marquez API JAR
```
cd ../..
./gradlew --no-daemon clean :api:shadowJar
```
PS: if you're getting an error like this: `BUG! exception in phase 'semantic analysis' in source unit '_BuildScript_' Unsupported class file major version 67` please check which is the latest Gradle version compatible with your Java version. Example: as I'm using Java 23, the current latest version compatible is 8.10 for Gradle.

```
ls -la api/build/libs
```
The output must be something like this:
```
total 96264
drwxr-xr-x@ 3 luis.yamada  staff        96 Dec 16 14:17 .
drwxr-xr-x@ 7 luis.yamada  staff       224 Dec 16 14:17 ..
-rw-r--r--@ 1 luis.yamada  staff  48787977 Dec 16 14:17 marquez-api-0.51.0-SNAPSHOT.jar
```
Use the JAR's name in the following commands for running the lifecycle routine.

## Run the **`CLI`**

Use the `--dry-run` flag to output an estimate of metadata deleted by the retention policy:

```bash
java -jar ../../api/build/libs/marquez-api-0.51.0-SNAPSHOT.jar db-retention \
  --number-of-rows-per-batch 1000 \
  --retention-days 3 \
  --dry-run \
  marquez.dbRetention.yml
```
The output must be something like this:
```
INFO  [2024-12-16 17:22:09,224] marquez.db.DbRetention: A retention policy of '1' days will delete (estimated): '0' jobs
INFO  [2024-12-16 17:22:09,441] marquez.db.DbRetention: A retention policy of '1' days will delete (estimated): '0' job versions
INFO  [2024-12-16 17:22:09,652] marquez.db.DbRetention: A retention policy of '1' days will delete (estimated): '0' runs
INFO  [2024-12-16 17:22:10,082] marquez.db.DbRetention: A retention policy of '1' days will delete (estimated): '0' datasets
INFO  [2024-12-16 17:22:10,721] marquez.db.DbRetention: A retention policy of '1' days will delete (estimated): '9' dataset versions
INFO  [2024-12-16 17:22:11,152] marquez.db.DbRetention: A retention policy of '1' days will delete (estimated): '0' lineage events
```

To run a  _one-off_ _ad-hoc_ retention policy on your metadata, use the [`db-retention`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/DbRetentionCommand.java) command:

```bash
java -jar ../../api/build/libs/marquez-api-0.51.0-SNAPSHOT.jar db-retention \
  --number-of-rows-per-batch 1000 \
  --retention-days 1 \
  marquez.dbRetention.yml
```
The output must be something like this:
```
INFO  [2024-12-16 17:40:19,678] marquez.db.DbRetention: Applying retention policy of '3' days to jobs...
INFO  [2024-12-16 17:41:04,201] marquez.db.DbRetention: Deleted '1839' jobs in '44520' ms!
INFO  [2024-12-16 17:41:04,202] marquez.db.DbRetention: Applying retention policy of '3' days to job versions...
INFO  [2024-12-16 17:41:08,232] marquez.db.DbRetention: Deleted '185' job versions in '4030' ms!
INFO  [2024-12-16 17:41:08,232] marquez.db.DbRetention: Applying retention policy of '3' days to runs...
INFO  [2024-12-16 17:41:37,619] marquez.db.DbRetention: Deleted '116483' runs in '29386' ms!
INFO  [2024-12-16 17:41:37,619] marquez.db.DbRetention: Applying retention policy of '3' days to datasets...
INFO  [2024-12-16 17:55:34,854] marquez.db.DbRetention: Deleted '2123' datasets in '837444' ms!
INFO  [2024-12-16 17:55:34,855] marquez.db.DbRetention: Applying retention policy of '3' days to dataset versions...
INFO  [2024-12-16 17:59:20,501] marquez.db.DbRetention: Deleted '137245' dataset versions in '225652' ms!
INFO  [2024-12-16 17:59:20,501] marquez.db.DbRetention: Applying retention policy of '3' days to lineage events...
INFO  [2024-12-16 18:00:30,231] marquez.db.DbRetention: Deleted '314193' lineage events in '69731' ms!
```