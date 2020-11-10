# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez-airflow/compare/0.3.7...HEAD)

## [0.3.7](https://github.com/MarquezProject/marquez-airflow/compare/0.3.6...0.3.7) - 2020-11-09

### Added

* Add support to get postgres table schemas
* Extract inputs / outputs from BigQuery jobs object (please see [Job](https://cloud.google.com/bigquery/docs/reference/rest/v2/Job) docs) [@julienledem](https://github.com/julienledem)

### Changed

* Bump `marquez-python` to [`0.7.11`](https://github.com/MarquezProject/marquez-python/releases/tag/0.7.11)

### Fixed

* Assure extractor associates only the output dataset on complete [@henneberger](https://github.com/henneberger)