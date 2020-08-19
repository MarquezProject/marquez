# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez/compare/0.11.1...HEAD)

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
