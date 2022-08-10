# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez/compare/0.25.0...HEAD)

## [0.25.0](https://github.com/MarquezProject/marquez/compare/0.24.0...0.25.0) - 2022-08-08

### Fixed

* Fix py module release [#2057](https://github.com/MarquezProject/marquez/pull/2057) [@wslulciuc](https://github.com/wslulciuc)
* Use /bin/sh in web/docker/entrypoint.sh [#2059](https://github.com/MarquezProject/marquez/pull/2059) [@wslulciuc](https://github.com/wslulciuc)

## [0.24.0](https://github.com/MarquezProject/marquez/compare/0.23.0...0.24.0) - 2022-08-02

### Added

* Add copyright lines to all source files [#1996](https://github.com/MarquezProject/marquez/pull/1996) [@merobi-hub](https://github.com/MarquezProject/marquez/commits?author=merobi-hub)
* Add copyright and license guidelines in CONTRIBUTING.md [@wslulciuc](https://github.com/wslulciuc)
* Add @FlywayTarget annotation to migration tests to control flyway upgrades [#2035](https://github.com/MarquezProject/marquez/pull/2035) [@collado-mike](https://github.com/collado-mike) 

### Changed

* Updated `jobs_view` to stop computing FQN on reads and to compute on _writes_ instead [#2036](https://github.com/MarquezProject/marquez/pull/2036) [@collado-mike](https://github.com/collado-mike)
* Runs row reduction [#2041](https://github.com/MarquezProject/marquez/pull/2041) [@collado-mike](https://github.com/collado-mike)

### Fixed

* Update `Run` in the openapi spec to include a `context` field [#2020](https://github.com/MarquezProject/marquez/pull/2020) [@esaych](https://github.com/Esaych)
* Fix dataset openapi model [#2038](https://github.com/MarquezProject/marquez/pull/2038) [@esaych](https://github.com/Esaych) 
* Fix casing on lastLifecycleState [#2039](https://github.com/MarquezProject/marquez/pull/2039) [@esaych](https://github.com/Esaych)
* Fix V45 migration to include initial population of jobs_fqn table [#2051](https://github.com/MarquezProject/marquez/pull/2051) [@collado-mike](https://github.com/collado-mike)
* Fix symlinked jobs in queries [#2053](https://github.com/MarquezProject/marquez/pull/2053) [@collado-mike](https://github.com/collado-mike)

## [0.23.0](https://github.com/MarquezProject/marquez/compare/0.22.0...0.23.0) - 2022-06-16

### Added

* Update docker-compose.yml: Randomly map postgres db port [#2000](https://github.com/MarquezProject/marquez/pull/2000) [@RNHTTR](https://github.com/RNHTTR) 
* Job parent hierarchy [#1935](https://github.com/MarquezProject/marquez/pull/1935) [#1980](https://github.com/MarquezProject/marquez/pull/1980) [#1992](https://github.com/MarquezProject/marquez/pull/1992) [@collado-mike](https://github.com/collado-mike)

### Changed

* Set default limit for listing datasets and jobs in UI from `2000` to `25` [#2018](https://github.com/MarquezProject/marquez/pull/2018) [@wslulciuc](https://github.com/wslulciuc)
* Update OpenLineage write API to be non-transactional and avoid unnecessary locks on records under heavy contention [@collado-mike](https://github.com/collado-mike)

### Fixed

* Return the tag for postgresql to 12.1.0 [#2015](https://github.com/MarquezProject/marquez/pull/2015) [@rossturk](https://github.com/rossturk)

## [0.22.0](https://github.com/MarquezProject/marquez/compare/0.21.0...0.22.0) - 2022-05-16

### Added

* Add support for `LifecycleStateChangeFacet` with an ability to softly delete datasets [#1847](https://github.com/MarquezProject/marquez/pull/1847)[@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
* Enable pod specific annotations in Marquez Helm Chart via `marquez.podAnnotations` [#1945](https://github.com/MarquezProject/marquez/pull/1945) [@wslulciuc](https://github.com/wslulciuc)
* Add support for job renaming/redirection via symlink [#1947](https://github.com/MarquezProject/marquez/pull/1947) [@collado-mike](https://github.com/collado-mike)
* Add `Created by` view for dataset versions along with SQL syntax highlighting in web UI [#1929](https://github.com/MarquezProject/marquez/pull/1929) [@phixMe](https://github.com/phixMe)
* Add `operationId` to openapi spec [#1978](https://github.com/MarquezProject/marquez/pull/1978) [@phixMe](https://github.com/phixMe)

### Changed

* Upgrade Flyway to v7.6.0 [#1974](https://github.com/MarquezProject/marquez/pull/1974) [@dakshin-k](https://github.com/dakshin-k)

### Fixed

* Remove size limits on namespaces, dataset names, and and source connection urls [#1925](https://github.com/MarquezProject/marquez/pull/1925) [@collado-mike](https://github.com/collado-mike)
* Update namespace names to allow `=`, `@`, and `;` [#1936](https://github.com/MarquezProject/marquez/pull/1936) [@mobuchowski](https://github.com/mobuchowski)
* Time duration display in web UI [#1950](https://github.com/MarquezProject/marquez/pull/1950) [@phixMe](https://github.com/phixMe)
* Enable web UI to access API via Helm Chart [@GZack2000](https://github.com/GZack2000)

## [0.21.0](https://github.com/MarquezProject/marquez/compare/0.20.0...0.21.0) - 2022-03-03

### Added

* Add MDC to the `LoggingMdcFilter` to include API method, path, and request ID [@fm100](https://github.com/fm100)
* Add Postgres sub-chart to Helm deployment for easier installation option [@KevinMellott91](https://github.com/KevinMellott91)
* GitHub Action workflow to validate changes to Helm chart [@KevinMellott91](https://github.com/KevinMellott91)

### Changed

* Upgrade from `Java11` to `Java17` [@ucg8j](https://github.com/ucg8j)
* Switch JDK image from `alpine` to [`temurin`](https://adoptium.net) enabling Marquez to run on multiple CPU architectures [@ucg8j](https://github.com/ucg8j)

### Fixed

* Error when running Marquez on Apple M1 [@ucg8j](https://github.com/ucg8j)

### Removed

* The `/api/v1-beta/lineage` endpoint [@wslulciuc](https://github.com/wslulciuc)
* The `marquez-airflow` lib. has been removed, **Please use the** [`openlineage-airflow`](https://pypi.org/project/openlineage-airflow) **library instead**. To migrate to using `openlineage-airflow`, make the following changes [@wslulciuc](https://github.com/wslulciuc):

    ```diff
    # Update the import in your DAG definitions
    -from marquez_airflow import DAG
    +from openlineage.airflow import DAG
    ```
    ```diff
    # Update the following environment variables in your Airflow instance
    -MARQUEZ_URL
    +OPENLINEAGE_URL
    -MARQUEZ_NAMESPACE
    +OPENLINEAGE_NAMESPACE
    ```
* The `marquez-spark` lib. has been removed. **Please use the** [`openlineage-spark`](https://search.maven.org/artifact/io.openlineage/openlineage-spark) **library instead**. To migrate to using `openlineage-spark`, make the following changes [@wslulciuc](https://github.com/wslulciuc):

    ```diff
    SparkSession.builder()
    - .config("spark.jars.packages", "io.github.marquezproject:marquez-spark:0.20.+")
    + .config("spark.jars.packages", "io.openlineage:openlineage-spark:0.2.+")
    - .config("spark.extraListeners", "marquez.spark.agent.SparkListener")
    + .config("spark.extraListeners", "io.openlineage.spark.agent.OpenLineageSparkListener")
      .config("spark.openlineage.host", "https://api.demo.datakin.com")
      .config("spark.openlineage.apiKey", "your datakin api key")
      .config("spark.openlineage.namespace", "<NAMESPACE_NAME>")
    .getOrCreate()
    ```

## [0.20.0](https://github.com/MarquezProject/marquez/compare/0.19.1...0.20.0) - 2021-12-13

### Added

* Add [deploy](https://marquezproject.github.io/marquez/deployment-overview.html) docs for running Marquez on AWS [@wslulciuc](https://github.com/wslulciuc) [@merobi-hub](https://github.com/merobi-hub)

### Changed

* Clarify docs on using OpenLineage for metadata collection [@fm100](https://github.com/fm100)
* Upgrade to gradle `7.x` [@wslulciuc](https://github.com/wslulciuc)
* Use `eclipse-temurin` for Marquez API base docker image [@fm100](https://github.com/fm100)

### Deprecated

* The following endpoints have been deprecated and are **scheduled to be removed in** `0.25.0`. Please use the [`/lineage`](https://marquezproject.github.io/marquez/openapi.html#tag/Lineage/paths/~1lineage/post) endpoint when collecting source, dataset, and job metadata [@wslulciuc](https://github.com/wslulciuc):
  * [`/sources`](https://marquezproject.github.io/marquez/openapi.html#tag/Sources/paths/~1sources~1{source}/put) endpoint to collect source metadata
  * [`/datasets`](https://marquezproject.github.io/marquez/openapi.html#tag/Datasets/paths/~1namespaces~1{namespace}~1datasets~1{dataset}/put) endpoint to collect dataset metadata
  * [`/jobs`](https://marquezproject.github.io/marquez/openapi.html#tag/Jobs/paths/~1namespaces~1{namespace}~1jobs~1{job}/put) endpoint to collect job metadata

### Fixed

* Validation of OpenLineage events on write [@collado-mike](https://github.com/collado-mike)
* Increase `name` column size for tables `namespaces` and `sources` [@mmeasic](https://github.com/mmeasic)

### Security

* Fix log4j [exploit](https://nvd.nist.gov/vuln/detail/CVE-2021-44228) [@fm100](https://github.com/fm100)

## [0.19.1](https://github.com/MarquezProject/marquez/compare/0.19.0...0.19.1) - 2021-11-05

### Fixed

* URI and URL DB mappper should handle empty string as null [@OleksandrDvornik](https://github.com/OleksandrDvornik)
* Fix NodeId parsing when dataset name contains `struct<>` [@fm100](https://github.com/fm100)
* Add encoding for dataset names in URL construction [@collado-mike](https://github.com/collado-mike)

## [0.19.0](https://github.com/MarquezProject/marquez/compare/0.18.0...0.19.0) - 2021-10-21

### Added

* Add simple python client example [@wslulciuc](https://github.com/wslulciuc)
* Display dataset versions in web UI :tada: [@phixMe](https://github.com/phixMe)
* Display runs and run facets in web UI :tada: [@phixMe](https://github.com/phixMe)
* Facet formatting and highlighting as Json in web UI [@phixMe](https://github.com/phixMe)
* Add option for `docker/up.sh` to run in the background [@rossturk](https://github.com/rossturk)
* Return `totalCount` in lists of jobs and datatsets [@phixMe](https://github.com/phixMe)

### Changed

* Change type column in `dataset_fields` table to `TEXT` [@wslulciuc](https://github.com/wslulciuc)
* Set `ZonedDateTime` parsing to support optional offsets and default to server timezone [@collado-mike](https://github.com/collado-mike)

### Fixed

* `Job.location` and `Source.connectionUrl` should be in URI format on write [@OleksandrDvornik](https://github.com/OleksandrDvornik)
* Z-Index fix for nodes and edges in lineage graph [@phixMe](https://github.com/phixMe)
* Format of the index files for web UI [@phixMe](https://github.com/phixMe)
* Fix OpenLineage API to return correct response codes for exceptions propagated from async calls [@collado-mike](https://github.com/collado-mike)
* Stopped overwriting nominal time information with nulls [@mobuchowski](https://github.com/mobuchowski)

### Removed

* `WriteOnly` clients for `java` and `python`. Before [OpenLineage](https://openlineage.io), we added a `WriteOnly` implementation to our clients to emit calls to a backend. A `backend` enabled collecting raw HTTP requests to an HTTP endpoint, console, or file. This was our way of capturing lineage _events_ that could then be used to automatically create resources on the Marquez backend. We soon worked on a standard that eventually became OpenLineage. That is, OpenLineage removed the need to make individual calls to create a namespace, a source, a datasets, etc, but rather accept an event with metadata that the backend could process. [@wslulciuc](https://github.com/wslulciuc)

## [0.18.0](https://github.com/MarquezProject/marquez/compare/0.17.0...0.18.0) - 2021-09-14

### Added

* **New** Add Search API :tada: [@wslulciuc](https://github.com/wslulciuc)
* Add `.env.example` to override variables defined in docker-compose files [@wslulciuc](https://github.com/wslulciuc)

### Changed

* Add [openlineage-java](https://search.maven.org/artifact/io.openlineage/openlineage-java) as dependency [@OleksandrDvornik](https://github.com/OleksandrDvornik)
* Move **class** SentryConfig from `marquez` to `marquez.tracing` pkg
* Major UI [improvements](https://github.com/MarquezProject/marquez/blob/main/web/docs/demo.gif); the UI now uses the Search and Lineage APIs  :tada: [@phixMe](https://github.com/phixMe)
* Set default API port to `8080` when running the Marquez shadow `jar` [@wslulciuc](https://github.com/wslulciuc)

### Fixed

* Update [`examples/airflow`](https://github.com/MarquezProject/marquez/tree/main/examples/airflow) to use `openlineage-airflow` and fix the SQL in DAG troubleshooting step [@wslulciuc](https://github.com/wslulciuc)

### Removed

* Drop `job_versions_io_mapping_inputs` and `job_versions_io_mapping_outputs` tables [@OleksandrDvornik](https://github.com/OleksandrDvornik)

## [0.17.0](https://github.com/MarquezProject/marquez/compare/0.16.1...0.17.0) - 2021-08-20

### Changed

* Update Lineage runs query to improve performance, added tests [@collado-mike](https://github.com/collado-mike)
* Add **POST** `/api/v1/lineage` endpoint to docs and **deprecate** run endpoints [@wslulciuc](https://github.com/wslulciuc)
* Drop `FieldType` enum [@wslulciuc](https://github.com/wslulciuc)

### Deprecated

* Run API [endpoints](https://marquezproject.github.io/marquez/openapi.html#tag/Jobs) that create or modify a job run (**scheduled to be removed in** `0.19.0`). Please use the **POST** `/api/v1/lineage` [endpoint](https://marquezproject.github.io/marquez/openapi.html#tag/Lineage/paths/~1lineage/post) when collecting job run metadata. [@wslulciuc](https://github.com/wslulciuc)
* Airflow integration, please use the [`openlineage-airflow`](https://pypi.org/project/openlineage-airflow) library instead. [@wslulciuc](https://github.com/wslulciuc)
* Spark integration, please use the [`openlineage-spark`](https://search.maven.org/artifact/io.openlineage/openlineage-spark) library instead. [@wslulciuc](https://github.com/wslulciuc)
* Write only clients for `java` and `python` (**scheduled to be removed in** `0.19.0`) [@wslulciuc](https://github.com/wslulciuc)

### Removed

* Dbt integration lib. [@wslulciuc](https://github.com/wslulciuc)
* Common integration lib. [@wslulciuc](https://github.com/wslulciuc)

## [0.16.1](https://github.com/MarquezProject/marquez/compare/0.16.0...0.16.1) - 2021-07-13

### Fixed

* dbt packages should look for namespace packages [@mobuchowski](https://github.com/mobuchowski)
* Add common integration dependency to dbt plugins [@mobuchowski](https://github.com/mobuchowski)
* `DatasetVersionDao` queries missing input and output facets [@dominiquetipton](https://github.com/dominiquetipton)
* (De)serialization issue for `Run` and `JobData` models [@collado-mike](https://github.com/collado-mike)
* Prefix spark `openlineage.*` configuration parameters with `spark.*` [@collado-mike](https://github.com/collado-mike)
* Parse multi-statement sql in **class** `SqlParser` used in Airflow integration [@wslulciuc](https://github.com/wslulciuc)
* URL-encode namespace on calls to API backend [@phixMe](https://github.com/phixMe)

## [0.16.0](https://github.com/MarquezProject/marquez/compare/0.15.2...0.16.0) - 2021-07-01

### Added

* **New** Add JobVersion API :tada: [@collado-mike](https://github.com/collado-mike)
* **New** Add DBT integrations for BigQuery and Snowflake :tada: [@mobuchowski](https://github.com/mobuchowski)

### Changed

* Reverted delete of BigQueryNodeVisitor to work with vanilla SparkListener [@collado-mike](https://github.com/collado-mike)
* Promote Lineage API out of beta [@OleksandrDvornik](https://github.com/OleksandrDvornik)

### Fixed

* Display job SQL in UI [@phixMe](https://github.com/phixMe)
* Allow upsert of tags [@hanbei](https://github.com/hanbei)
* Allow potentially ambiguous URIs with encoded path segments [@mobuchowski](https://github.com/mobuchowski)
* Use source [naming convetion](https://github.com/OpenLineage/OpenLineage/blob/main/spec/Naming.md) defined by OpenLineage [@mobuchowski](https://github.com/mobuchowski)
* Return dataset facets [@collado-mike](https://github.com/collado-mike)
* BigQuery source naming in integrations [@mobuchowski](https://github.com/mobuchowski)

## [0.15.2](https://github.com/MarquezProject/marquez/compare/0.15.1...0.15.2) - 2021-06-17

### Added

* Add endpoint to create tags [@hanbei](https://github.com/hanbei)

### Fixed

* Fixed build & release process for python marquez-integration-common package [@collado-mike](https://github.com/collado-mike)
* Fixed snowflake and bigquery errors when connector libraries not loaded [@collado-mike](https://github.com/collado-mike)
* Fixed Openlineage API does not set Dataset current_version_uuid #1361 [@collado-mike](https://github.com/collado-mike)

## [0.15.1](https://github.com/MarquezProject/marquez/compare/0.15.0...0.15.1) - 2021-06-11

### Added

* Factored out common functionality in Python airflow integration [@mobuchowski](https://github.com/mobuchowski)
* Added Airflow task run macro to expose task run id [@collado-mike](https://github.com/collado-mike)

### Changed

* Refactored ValuesAverageExpectationParser to ValuesSumExpectationParser and ValuesCountExpectationParser [@collado-mike](https://github.com/collado-mike)
* Updated SparkListener to extend Spark's SparkListener abstract class [@collado-mike](https://github.com/collado-mike)

### Fixed

* Use current project version in spark openlineage client [@mobuchowski](https://github.com/mobuchowski)
* Rewrote LineageDao queries and LineageService for performance [@collado-mike](https://github.com/collado-mike)
* Updated lineage query to include new jobs that have no job version yet [@collado-mike](https://github.com/collado-mike)

## [0.15.0](https://github.com/MarquezProject/marquez/compare/0.14.2...0.15.0) - 2021-05-24

### Added

* Add tracing visibility [@julienledem](https://github.com/julienledem)
* **New** Add snowflake extractor :tada: [@mobuchowski](https://github.com/mobuchowski)
* Add SSLContext to MarquezClient [@lewiesnyder](https://github.com/lewiesnyder)
* Add support for LogicalRDDs in spark plan visitors [@collado-mike](https://github.com/collado-mike)
* **New** Add Great Expectations based data quality facet support :tada: [@mobuchowski](https://github.com/mobuchowski)

### Changed

* Augment tutorial instructions & screenshots for Airflow example [@rossturk](https://github.com/rossturk)
* Rewrite correlated subqueries when querying the lineage_events table [@collado-mike](https://github.com/collado-mike)

###  Fixed

* Web time formatting display fix [@kachontep](https://github.com/kachontep)

## [0.14.2](https://github.com/MarquezProject/marquez/compare/0.14.1...0.14.2) - 2021-05-06

### Changed

* Unpin `requests` dep in `marquez-airflow` integration [@wslulciuc](https://github.com/wslulciuc)
* Unpin `attrs` dep in `marquez-airflow` integration [@wslulciuc](https://github.com/wslulciuc)

## [0.14.1](https://github.com/MarquezProject/marquez/compare/0.14.0...0.14.1) - 2021-05-05

### Changed

* Updated dataset lineage query to find most recent job that wrote to it [@collado-mike](https://github.com/collado-mike)
* Pin http-proxy-middleware to 0.20.0 [@wslulciuc](https://github.com/wslulciuc)

## [0.14.0](https://github.com/MarquezProject/marquez/compare/0.13.1...0.14.0) - 2021-05-03

### Added

*  GA tag for website tracking [@rossturk](https://github.com/rossturk)
*  Basic CTE support in `marquez-airflow` [@mobuchowski](https://github.com/mobuchowski)
*  Airflow custom facets, bigquery statistics facets [@mobuchowski](https://github.com/mobuchowski)
*  Unit tests for **class** [`JobVersionDao`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/JobVersionDao.java) [@wslulciuc](https://github.com/wslulciuc)
*  Sentry tracing support [@julienledem](https://github.com/julienledem)
*  OpenLineage facets support to API response models :tada: [@wslulciuc](https://github.com/wslulciuc)

### Changed

*  `BigQueryRelationTransformer` and deleted `BigQueryNodeVisitor` [@collado-mike](https://github.com/collado-mike)
*  Bump postgres to `12.1.0` [@wslulciuc](https://github.com/wslulciuc)
*  Update spark job name to reflect spark application name and execution node [@collado-mike](https://github.com/collado-mike)
*  Update `marquez-airflow` integration to use [OpenLineage](https://github.com/OpenLineage/OpenLineage) :tada: [@mobuchowski](https://github.com/mobuchowski)
*  Migrate tests to junit 5 [@mobuchowski](https://github.com/mobuchowski)
*  Rewrite lineage IO sql queries to avoid job_versions_io_mapping_* tables [@collado-mike](https://github.com/collado-mike)
*  Updated OpenLineage impl to only update dataset version on run completion [@collado-mike](https://github.com/collado-mike)

## [0.13.1](https://github.com/MarquezProject/marquez/compare/0.13.0...0.13.1) - 2021-04-01

### Changed

* Remove unused implementation of SQL parser in `marquez-airflow` [@mobuchowski](https://github.com/mobuchowski)

### Fixed

* Add inputs and outputs to lineage graph [@henneberger](https://github.com/henneberger)
* Updated `NodeId` regex to support URIs with scheme and ports [@collado-mike](https://github.com/collado-mike)

## [0.13.0](https://github.com/MarquezProject/marquez/compare/0.12.2...0.13.0) - 2021-03-30

### Added

* Secret support for helm chart [@KevinMellott91](https://github.com/KevinMellott91)
* **New** `seed` cmd to populate `marquez` database with source, dataset, and job metadata allowing users to try out features of Marquez (data lineage, view job run history, etc) :tada:
* Docs on applying db migrations manually
* **New** Lineage API to support data lineage queries :tada:
* Support for logging errors via [sentry](https://sentry.io/)
* **New** Airflow [example](https://github.com/MarquezProject/marquez/tree/main/examples/airflow) with Marquez :tada:

### Changed

* Update OpenLinageDao to stop converting URI structures to contain underscores instead of colons and slashes [@collado-mike](https://github.com/collado-mike)
* Bump testcontainers dependency to `v1.15.2` [@ ShakirzyanovArsen](https://github.com/ShakirzyanovArsen)
* Register output datasets for a run lazily [@henneberger](https://github.com/henneberger)
* Refactor spark plan traversal to find input/output datasets from datasources [@collado-mike](https://github.com/collado-mike)
* Web UI project settings and default marquez port [@phixMe](https://github.com/phixMe)
* Associate dataset inputs on run start [@henneberger](https://github.com/henneberger)

### Fixed

* Dataset description is not overwritten on update [@henneberger](https://github.com/henneberger)
* Latest tags are returned from dataset [@henneberger](https://github.com/henneberger)
* Airflow integration tests on forked PRs [@mobuchowski](https://github.com/mobuchowski)
* Empty nominal end time support [@henneberger](https://github.com/henneberger)
* Ensure valid dataset fields for OpenLineage [@henneberger](https://github.com/henneberger)
* Ingress context templating for helm chart [@KulykDmytro](https://github.com/KulykDmytro)

## [0.12.2](https://github.com/MarquezProject/marquez/compare/0.12.0...0.12.2) - 2021-03-16

### Changed

* Use alpine image for `marquez` reducing image size by `+50%` [@KevinMellott91](https://github.com/KevinMellott91)
* Use alpine image for `marquez-web` reducing image size by `+50%` [@KevinMellott91](https://github.com/KevinMellott91)

### Fixed

* Ensure `marquez.DAG` is (de)serializable

## [0.12.0](https://github.com/MarquezProject/marquez/compare/0.11.2...0.12.0) - 2021-02-08

### Added

* Modules: [`api`](https://github.com/MarquezProject/marquez/tree/main/api), [`web`](https://github.com/MarquezProject/marquez/tree/main/web), [`clients `](https://github.com/MarquezProject/marquez/tree/main/clients), [`chart`](https://github.com/MarquezProject/marquez/tree/main/chart), and [`integrations`](https://github.com/MarquezProject/marquez/tree/main/integrations)
* Working airflow example
* `runs` table indices for columns: `created_at` and `current_run_state` [@phixMe](https://github.com/phixMe)
*  New `/lineage` endpoint for [OpenLineage](https://github.com/OpenLineage/OpenLineage) support [@henneberger](https://github.com/henneberger)
*  New graphql endpoint [@henneberger](https://github.com/henneberger)
*  New spark integration [@henneberger](https://github.com/henneberger)
*  New API to list versions for a dataset

### Changed

* Drop `Source.type` enum (now a _string_ type)

### Fixed

* Replace `jdbi.getHandle()` with `jdbi.withHandle()` to free DB connections from pool [@henneberger](https://github.com/henneberger)
* Fix `RunListener` when registering outside of the `MarquezContext` builder [@henneberger](https://github.com/henneberger)

## [0.11.3](https://github.com/MarquezProject/marquez/compare/0.11.2...0.11.3) - 2020-11-02

### Added

* Add support for external ID on run creation [@julienledem](https://github.com/julienledem)
* Throw `RunAlreadyExistsException` on run ID already exists
* Add BigQuery, Pulsar, and Oracle source types [@sreev](https://github.com/sreev)
* Add run ID support in job meta; the optional run ID will be used to link a newly created job version to an existing job run, while supporting updating the run state and avoiding having to create another run

### Fixed

* Use `postgres` instead of `db` in [`marquez.dev.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.dev.yml)
* Allow multiple postgres containers in test suite [@phixMe](https://github.com/phixMe)

## [0.11.2](https://github.com/MarquezProject/marquez/compare/0.11.1...0.11.2) - 2020-08-21

### Changed

* Always migrate db schema on app start in development config
* Update default db username / password
* Use [`marquez.dev.yml`](https://github.com/MarquezProject/marquez/blob/main/marquez.dev.yml) in on docker compose `up`

## [0.11.1](https://github.com/MarquezProject/marquez/compare/0.11.0...0.11.1) - 2020-08-19

### Added

* Use shorten name for namespaces in version IDs
* Add namespace to Dataset and Job models
* Add ability to deserialize `int` type to columns [@phixMe](https://github.com/phixMe)
* Add `SqlLogger` for SQL profiling
* Add `DatasetVersionId.asDatasetId()` and `JobVersionId.asJobId()`
* Add `DatasetService.getBy(DatasetVersionId): Dataset`
* Add `JobService.getBy(JobVersionId): Job`
* Allow for run transition override via `at=<TIMESTAMP>`, where `TIMESTMAP` is an ISO 8601 timestamp representing the date/time of the [state](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/common/models/RunState.java) transition. For example:

  ```
  POST /jobs/runs/{id}/start?at=<TIMESTAMP>
  ```

### Changed

* `config.yml` -> `marquez.yml`

### Fixed

* Fix dataset version column mappings

## [0.11.0](https://github.com/MarquezProject/marquez/compare/0.10.4...0.11.0) - 2020-05-27

### Added

* `Run.startedAt`, `Run.endedAt`, `Run.duration` [@julienledem](https://github.com/julienledem)
* **class** [`MarquezContext`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/MarquezContext.java) [@julienledem](https://github.com/julienledem)
* **class** [`RunTransitionListener`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/service/RunTransitionListener.java) [@julienledem](https://github.com/julienledem)
* Unique identifier **class** [`DatasetId`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/common/models/DatasetId.java) for datasets [@julienledem](https://github.com/julienledem)
* Unique identifier **class** [`JobId`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/common/models/JobId.java) for jobs [@julienledem](https://github.com/julienledem)
* **class** [`RunId`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/common/models/RunId.java) [@ravikamaraj](https://github.com/ravikamaraj)
* **enum** [`RunState`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/common/models/RunState.java) [@ravikamaraj](https://github.com/ravikamaraj)
* **class** [`Version`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/service/models/Version.java) [@ravikamaraj](https://github.com/ravikamaraj)

### Changed

* Job inputs / outputs are defined as [`DatasetId`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/common/models/DatasetId.java)
* Bump to JDK 11

### Removed

* Use of API models under `marquez.api.models` pkg

### Fixed

* API docs example to show correct `SQL` key in job context [@frankcash](https://github.com/frankcash)

## [0.10.4](https://github.com/MarquezProject/marquez/compare/0.10.3...0.10.4) - 2020-01-17

### Fixed

* Fix `RunState.isComplete()`

## [0.10.3](https://github.com/MarquezProject/marquez/compare/0.10.2...0.10.3) - 2020-01-17

### Added

* Add new logo
* Add `JobResource.locationFor()`

### Fixed

* Fix dataset field versioning
* Fix list job runs

## [0.10.2](https://github.com/MarquezProject/marquez/compare/0.10.1...0.10.2) - 2020-01-16

### Added

* Added Location header to run creation [@nkijak](https://github.com/nkijak)

## [0.10.1](https://github.com/MarquezProject/marquez/compare/0.10.0...0.10.1) - 2020-01-11

### Changed

* Rename `datasets.last_modified`

## [0.10.0](https://github.com/MarquezProject/marquez/compare/0.9.2...0.10.0) - 2020-01-08

### Changed

* Rename table `dataset_tag_mapping`

## [0.9.2](https://github.com/MarquezProject/marquez/compare/0.9.1...0.9.2) - 2020-01-07

### Added

* Add `Flyway.baselineOnMigrate` flag

## [0.9.1](https://github.com/MarquezProject/marquez/compare/0.9.0...0.9.1) - 2020-01-06

### Added

* Add redshift data types
* Add links to dropwizard overrides in `config.yml`

## [0.9.0](https://github.com/MarquezProject/marquez/compare/0.8.0...0.9.0) - 2020-01-05

### Added

* Validate `runID` when linked to dataset change
* Add `Utils.toUuid()`
* Add tests for class `TagDao`
* Add default tags to config
* Add tagging support for dataset fields
* Add `docker/config.dev.yml`
* Add flyway config support

### Changed

* Replace deprecated `App.onFatalError()`

### Fixed

* Fix error on tag exists
* Fix malformed sql in `RunDao.findAll()`

## [0.8.0](https://github.com/MarquezProject/marquez/compare/0.7.0...0.8.0) - 2019-12-12

### Added

* Add `Dataset.lastModified``
* Add `tags` table schema
* Add **`GET`** `/tags`

### Changed

* Use new Flyway version to fix migration with custom roles
* Modify `args` column in table `run_args

## [0.7.0](https://github.com/MarquezProject/marquez/compare/0.6.0...0.7.0) - 2019-12-05

### Added

* Link dataset versions with run inputs
* Add schema required by tagging
* More tests for class `common.Utils`
* Add `ColumnsTest`
* Add `RunDao.insert()`
* Add `RunStateDao.insert()`
* Add [`METRICS.md`](https://github.com/MarquezProject/marquez/blob/main/METRICS.md)
* Add prometheus dep and expose **`GET`** `/metrics`

### Fixed

* Fix dataset field serialization

## [0.6.0](https://github.com/MarquezProject/marquez/compare/0.5.1...0.6.0) - 2019-11-29

### Added

* Add `Job.latestRun`
* Add debug logging

### Changed

* Adjust class RunResponse property ordering on serialization
* Update logging on default namespace creation

## [0.5.1](https://github.com/MarquezProject/marquez/compare/0.5.0...0.5.1) - 2019-11-20

### Added

* Add dataset field versioning support
* Add link to web UI
* Add `Job.context`

### Changed

* Update semver regex in build-and-push.sh
* Minor updates to job and dataset versioning functions
* Make `Job.location` optional

## [0.5.0](https://github.com/MarquezProject/marquez/compare/0.4.0...0.5.0) - 2019-11-04

### Added

* Add `lombok.config`
* Add code review guidelines
* Add `JobType`
* Add limit and offset support to NamespaceAPI
* Add Development section to `CONTRIBUTING.md`
* Add class **`DatasetMeta`**
* Add class **`MorePreconditions`**
* Added install instructions for docker

### Changed

* Rename guid column to uuid
* Use admin ping and health
* Update `owner` to `ownerName`

### Removed

* Remove experimental db table versioning code

### Fixed

* Fix `marquez.jar` rename on `COPY`

## [0.4.0](https://github.com/MarquezProject/marquez/compare/0.3.4...0.4.0) - 2019-06-04

### Added

* Add quickstart
* Add **`GET`** `/namespaces/{namespace}/jobs/{job}/runs`

## [0.3.4](https://github.com/MarquezProject/marquez/compare/0.3.3...0.3.4) - 2019-05-17

### Changed

* Change `Datasetdao.findAll()` to order by `Dataset.name`

## [0.3.3](https://github.com/MarquezProject/marquez/compare/0.3.2...0.3.3) - 2019-05-14

### Changed

* Set timestamps to `CURRENT_TIMESTAMP`

## [0.3.2](https://github.com/MarquezProject/marquez/compare/0.3.1...0.3.2) - 2019-05-14

### Changed

* Set `job_versions.updated_at` to `CURRENT_TIMESTAMP`

## [0.3.1](https://github.com/MarquezProject/marquez/compare/0.3.0...0.3.1) - 2019-05-14

### Added

* Handle `Flyway.repair()` error

## [0.3.0](https://github.com/MarquezProject/marquez/compare/0.2.1...0.3.0) - 2019-05-14

### Added

* Add `JobResponse.updatedAt`

### Changed

* Return timestamp strings as ISO format

### Removed

* Remove unused tables in db schema


## [0.2.1](https://github.com/MarquezProject/marquez/compare/0.2.0...0.2.1) - 2019-04-22

### Changed

* Support dashes (`-`) in namespace

## [0.2.0](https://github.com/MarquezProject/marquez/compare/0.1.0...0.2.0) - 2019-04-15

### Added

* Add `@NoArgsConstructor` to exceptions
* Add license to `*.java`
* Add column constants
* Add response/error metrics to API endpoints
* Add build info to jar manifest
* Add release steps and plugin
* Add `/jobs/runs/{id}/run`
* Add jdbi metrics
* Add gitter link
* Add column constants
* Add **`MarquezServiceException`**
* Add `-parameters` compiler flag
* Add JSON logging support

### Changed

* Minor pkg restructuring
* Throw [**`NamespaceNotFoundException`**](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/api/exceptions/NamespaceNotFoundException.java) on [`NamespaceResource.get()`](https://github.com/MarquezProject/marquez/blob/main/src/main/java/marquez/api/NamespaceResource.java#L80)

### Fixed

* Fix dataset list error

## [0.1.0](https://github.com/MarquezProject/marquez/releases/tag/0.1.0) - 2018-12-18

* Marquez initial public release.
