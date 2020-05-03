# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez/compare/master...HEAD)

### Added

## [0.3.0](https://github.com/MarquezProject/marquez/releases/tag/0.3.0) - 2019-05-14

### Added

* Add JobResponse.updatedAt

### Changed

* Return timestamp strings as ISO format 

### Removed

* Remove unused tables in db schema


## [0.2.1](https://github.com/MarquezProject/marquez/releases/tag/0.2.1) - 2019-04-22

### Changed

* Support dashes (`-`) in namespace

## [0.2.0](https://github.com/MarquezProject/marquez/releases/tag/0.2.0) - 2019-04-15

### Added

* Add build info to jar manifest
* Add release steps and plugin
* Add `/jobs/runs/{id}/run`
* Add jdbi metrics
* Add gitter link
* Add column constants
* Add MarquezServiceException
* Add -parameters compiler flag
* Add JSON logging support

### Changed

* Minor pkg restructuring
* Throw [**`NamespaceNotFoundException`**](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/api/exceptions/NamespaceNotFoundException.java) on [`NamespaceResource.get()`](https://github.com/MarquezProject/marquez/blob/master/src/main/java/marquez/api/NamespaceResource.java#L80) 

## [0.1.0](https://github.com/MarquezProject/marquez/releases/tag/0.1.0) - 2018-12-18

* Marquez initial public release.
