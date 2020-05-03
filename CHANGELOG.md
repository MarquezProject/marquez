# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez/compare/master...HEAD)

### Added

## [0.7.0](https://github.com/MarquezProject/marquez/releases/tag/0.7.0) - 2019-12-05

### Added

* Link dataset versions with run inputs 
* Add schema required by tagging
* More tests for class `common.Utils`
* Add `ColumnsTest`
* Add `RunDao.insert()`
* Add `RunStateDao.insert()`
* Add custom metrics
* Add prometheus dep and expose metrics endpoint

### Fixed

* Fix dataset field serialization 

## [0.6.0](https://github.com/MarquezProject/marquez/releases/tag/0.6.0) - 2019-11-29

### Added

* Add `Job.latestRun`
* Add debug logging 

### Changed

* Adjust class RunResponse property ordering on serialization
* Update logging on default namespace creation

## [0.5.1](https://github.com/MarquezProject/marquez/releases/tag/0.5.1) - 2019-11-20

### Added

* Add dataset field versioning support
* Add link to web UI
* Add `Job.context`

### Changed

* Update semver regex in build-and-push.sh
* Minor updates to job and dataset versioning functions
* Make `Job.location` optional 

## [0.5.0](https://github.com/MarquezProject/marquez/releases/tag/0.5.0) - 2019-11-04

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
* pdate owner to ownerName

### Removed

* Remove experimental db table versioning code

### Fixed

* Fix `marquez.jar` rename on `COPY` 

## [0.4.0](https://github.com/MarquezProject/marquez/releases/tag/0.4.0) - 2019-06-04

### Added

* Add quickstart
* Add **`GET`** `/namespaces/{namespace}/jobs/{job}/runs`

## [0.3.4](https://github.com/MarquezProject/marquez/releases/tag/0.3.4) - 2019-05-17

### Changed

* Change `Datasetdao.findAll()` to order by `Dataset.name`

## [0.3.3](https://github.com/MarquezProject/marquez/releases/tag/0.3.3) - 2019-05-14

### Changed

* Set timestamps to `CURRENT_TIMESTAMP` 

## [0.3.2](https://github.com/MarquezProject/marquez/releases/tag/0.3.2) - 2019-05-14

### Changed

* Set `job_versions.updated_at` to `CURRENT_TIMESTAMP` 

## [0.3.1](https://github.com/MarquezProject/marquez/releases/tag/0.3.1) - 2019-05-14

### Added

* Handle `Flyway.repair()` error 

## [0.3.0](https://github.com/MarquezProject/marquez/releases/tag/0.3.0) - 2019-05-14

### Added

* Add `JobResponse.updatedAt`

### Changed

* Return timestamp strings as ISO format 

### Removed

* Remove unused tables in db schema


## [0.2.1](https://github.com/MarquezProject/marquez/releases/tag/0.2.1) - 2019-04-22

### Changed

* Support dashes (`-`) in namespace

## [0.2.0](https://github.com/MarquezProject/marquez/releases/tag/0.2.0) - 2019-04-15

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
* Throw [**`NamespaceNotFoundException`**](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/api/exceptions/NamespaceNotFoundException.java) on [`NamespaceResource.get()`](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/api/NamespaceResource.java#L80)

### Fixed

* Fix dataset list error

## [0.1.0](https://github.com/MarquezProject/marquez/releases/tag/0.1.0) - 2018-12-18

* Marquez initial public release.
