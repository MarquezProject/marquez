# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez-java/compare/0.4.3...HEAD)

## [0.4.3](https://github.com/MarquezProject/marquez-java/compare/0.4.2...0.4.3) - 2020-10-27

### Added

* `MarquezWriteOnlyClient` [@julienledem](https://github.com/julienledem)
* `LoggingBackend` [@julienledem](https://github.com/julienledem)
* Optional `runID` to job meta
* Support for optional api key to authenticate requests

### Changed

* Factor out path building logic [@julienledem](https://github.com/julienledem)

## [0.4.2](https://github.com/MarquezProject/marquez-java/compare/0.4.1...0.4.2) - 2020-08-14

### Changed

* Require namespace on all method calls

## [0.4.1](https://github.com/MarquezProject/marquez-java/compare/0.4.0...0.4.1) - 2020-07-24

### Added

* Run transition timestamp override via query param 
* Namespace to Dataset and Job models 
* Input/output string support to `JobMeta.Builder` 
* `Field.Builder`
* `MarquezClient.marRunAs()`
* `MarquezClient.createRun()` with namespace override

## [0.4.0](https://github.com/MarquezProject/marquez-java/compare/0.3.0...0.4.0) - 2020-05-24

### Added

* Tag API
* `DatasetID` and `JobID`
* `Job.latestRun`

## [0.3.0](https://github.com/MarquezProject/marquez-java/compare/0.2.0...0.3.0) - 2020-01-02

### Added

* `Dataset.lastModified` [@soumyasmruti](https://github.com/soumyasmruti)

## [0.2.0](https://github.com/MarquezProject/marquez-java/compare/0.1.0...0.2.0) - 2019-11-12

### Added

* Class `MarquezClientException`
* Class `MarquezHttpException`
* Class `JsonUtils`
* New meta model generators

### Changed

* List supported gradle tasks only in CONTRIBUTING.md
* Update example usage in README.md
* Remove the use of `@JsonProperty` by passing `-parameters` compiler flag
* Refactor class `MarquezClient`
* Refactor class `MarquezHttp`
* Refactor exception handling
* Update models to translate to/from json using factory methods
* Simplified test suite

## [0.1.0](https://github.com/MarquezProject/marquez-java/releases/tag/0.1.0) - 2019-08-13

* Initial public release
