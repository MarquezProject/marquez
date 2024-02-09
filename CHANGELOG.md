# Changelog

## [Unreleased](https://github.com/MarquezProject/marquez/compare/0.44.0...HEAD)

## [0.44.0](https://github.com/MarquezProject/marquez/compare/0.43.1...0.44.0) - 2024-01-22

### Added

* Web: add dataset tags tabs for adding/deleting of tags [`#2714`](https://github.com/MarquezProject/marquez/pull/2714) [@davidsharp7](https://github.com/davidsharp7)  
    *Adds a dataset tags component so that datasets can have tags added/deleted.*
* API: Add endpoint to delete field-level tags [`#2705`](https://github.com/MarquezProject/marquez/pull/2705) [@davidsharp7](https://github.com/davidsharp7)  
    *Adds delete endpoint to remove dataset field tags.*

### Fixed

* Web: fix dataset tag reducers bug [`#2716`](https://github.com/MarquezProject/marquez/pull/2716) [@davidsharp7](https://github.com/davidsharp7)  
    *Removes result from dataset tags reducer to fix a sidebar bug.*

## [0.43.1](https://github.com/MarquezProject/marquez/compare/0.43.0...0.43.1) - 2023-12-20

### Fixed

* API: fix broken lineage graph for multiple runs of the same job [`#2710`](https://github.com/MarquezProject/marquez/pull/2710) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Problem: lineage graph was not available for jobs run multiple times of the same job as a result of bug introduced in `0.43.0`.
    In order to fix the inconsistent data, [this `UPDATE` query](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/migrations/V67_2_JobVersionsIOMappingBackfillJob.java#L19)
    should be run. This is **not** required when upgrading directly to `0.43.1`.*

## [0.43.0](https://github.com/MarquezProject/marquez/compare/0.42.0...0.43.0) - 2023-12-15

### Added

* API: refactor the `RunDao` SQL query [`#2685`](https://github.com/MarquezProject/marquez/pull/2685) [@sophiely](https://github.com/sophiely)
    *Improves the performance of the SQL query used for listing all runs.*
* API: refactor dataset version query [`#2683`](https://github.com/MarquezProject/marquez/pull/2683) [@sophiely](https://github.com/sophiely)
    *Improves the performance of the SQL query used for the dataset version.*
* API: add support for a `DatasetEvent` [`#2641`](https://github.com/MarquezProject/marquez/pull/2641) [`#2654`](https://github.com/MarquezProject/marquez/pull/2654) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Adds a feature for saving into the Marquez model datasets sent via the `DatasetEvent` event type. Includes optimization of the lineage query.*
* API: add support for a `JobEvent` [`#2661`](https://github.com/MarquezProject/marquez/pull/2661) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Adds a feature for saving into the Marquez model jobs and datasets sent via the `JobEvent` event type.*
* API: add support for streaming jobs [`#2682`](https://github.com/MarquezProject/marquez/pull/2682) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Creates job version and reference rows at the beginning of the job instead of on complete. Updates the job version within the run if anything changes.*
* API/spec: implement upstream run-level lineage [`#2658`](https://github.com/MarquezProject/marquez/pull/2658) [@julienledem](https://github.com/julienledem)
    *Returns the version of each job and dataset a run is depending on.*
* API: add `DELETE` endpoint for dataset tags [`#2698`](https://github.com/MarquezProject/marquez/pull/2698) [@davidsharp7](https://github.com/davidsharp7)
    *Creates a new endpoint for removing the linkage between a dataset and a tag in `datasets_tag_mapping` to supply a way to delete a tag from a dataset via the API.
* Web: add a dataset drawer [`#2672`](https://github.com/MarquezProject/marquez/pull/2672) [@davidsharp7](https://github.com/davidsharp7)
    *Adds a drawer to the dataset column view in the GUI.*

### Fixed

* Client/Java: change url path encoding to match jersey decoding [`#2693`](https://github.com/MarquezProject/marquez/pull/2693) [@davidjgoss](https://github.com/davidjgoss)
    *Swaps out the implementation of `MarquezPathV1::encode` to use the `UrlEscapers` path segment escaper, which does proper URI encoding.*
* Web: fix pagination in the Jobs route [`#2655`](https://github.com/MarquezProject/marquez/pull/2655) [@merobi-hub](https://github.com/merobi-hub)
    *Hides job pagination in the case of no jobs.*
* Web: fix empty search experience [`#2679`](https://github.com/MarquezProject/marquez/pull/2679) [@phixMe](https://github.com/phixMe)
    *Use of the previous search value was resulting in a bad request for the first character of a search.*

### Removed

* Client/Java: remove maven-archiver dependency from the Java client [`#2695`](https://github.com/MarquezProject/marquez/pull/2695) [@davidjgoss](https://github.com/davidjgoss)
    *Removes a dependency from `build.gradle` that was bringing some transitive vulnerabilities.*

## [0.42.0](https://github.com/MarquezProject/marquez/compare/0.41.0...0.42.0) - 2023-10-17

### Added

* Client: add Java client method for dataset/job lineage [`#2623`](https://github.com/MarquezProject/marquez/pull/2623) [@davidjgoss](https://github.com/davidjgoss)
    *To add a method for the dataset/job-level endpoint (`GET /lineage`) to the Java SDK, this adds a new method to the `MarquezClient` for the endpoint, along with tests, and the necessary new subclasses of `NodeData` for datasets and jobs.*
    *Users currently employing the existing `getColumnLineage` method should upgrade and deploy their instance of the client first, then Marquez itself, for backwards compatibility.*
* Web: add IO tab [`#2613`](https://github.com/MarquezProject/marquez/pull/2613) [@phixme](https://github.com/phixMe)
    *Improves experience with large graphs by adding a new tab to move between graph elements without looking at the graph itself.*
* Web: add hover-over Tag tooltip to datasets [`#2630`](https://github.com/MarquezProject/marquez/pull/2630) [@davidsharp7](https://github.com/davidsharp7)
    *For parity with columns in the GUI, this adds a Tag tooltip to datasets.*
* API: upstream run-level lineage API [`#2658`](https://github.com/MarquezProject/marquez/pull/2658) [@julienledem]( https://github.com/julienledem)
    *When trouble shooting an issue and doing root cause analysis, it is usefull to get the upstream run-level lineage to know exactly what version of each job and dataset a run is depending on.*

### Changed

* Docker: upgrade to Docker Compose V2 [`#2644`](https://github.com/MarquezProject/marquez/pull/2644) [@merobi-hub](https://github.com/merobi-hub)
    *Docker Compose V1 has been at EOL since June, but docker/up.sh uses the V1 format. This upgrades the `up` command in up.sh to V2.*

### Removed

* API: drop table `job_contexts` and usage [`#2621`](https://github.com/MarquezProject/marquez/pull/2621) [@wslulciuc](https://github.com/wslulciuc)
    *Removes usage of `job_contexts`, which has been replaced by OpenLineage facets, and adds a migration to drop the table.*
* API: remove usage of `current_job_context_uuid` column [`#2622`](https://github.com/MarquezProject/marquez/pull/2622) [@wslulciuc](https://github.com/wslulciuc)
    *Removes usage of `job_context_uuid` and `current_job_context_uuid`. Column to be removed in 0.43.0.*

### Fixed

* Web: fix Unix epoch time display for null `endedAt` values [`#2647`](https://github.com/MarquezProject/marquez/pull/2647) [@merobi-hub](https://github.com/merobi-hub)
    *Fixes the issue of the GUI displaying Unix epoch time (midnight on January 1, 1970) in the case of running jobs/null `endedAt` values.*

## [0.41.0](https://github.com/MarquezProject/marquez/compare/0.40.0...0.41.0) - 2023-09-20

### Added

* API: add support for the following parameters in the `SearchDao` [`#2556`](https://github.com/MarquezProject/marquez/pull/2556) [@tati](https://github.com/tati) [@wslulciuc](https://github.com/wslulciuc)
    *This PR updates the search endpoint to enforce `YYYY-MM-DD` for query params, use `YYYY-MM-DD` as `LocalDate`, and support the following query params:*
    - *`namespace` - matches jobs or datasets within the given namespace.*
    - *`before` - matches jobs or datasets before `YYYY-MM-DD`.*
    - *`after` - matches jobs or datasets after `YYYY-MM-DD`.*
* Web: add paging on jobs and datasets [`#2614`](https://github.com/MarquezProject/marquez/pull/2614) [@phixme](https://github.com/phixMe)
    *Adds paging to jobs and datasets just like we already have on the lineage events page.*
* Web: add tag descriptions to tooltips [`#2612`](https://github.com/MarquezProject/marquez/pull/2612) [@davidsharp7](https://github.com/davidsharp7)
    *Get the tag descriptions from the tags endpoint and when a column has a tag display the corresponding description on hover over. Context can be found [here](https://docs.github.com/en/issues/tracking-your-work-with-issues/linking-a-pull-request-to-an-issue).*
* Web: add available column-level tags [`#2606`](https://github.com/MarquezProject/marquez/pull/2606) [@davidsharp7](https://github.com/davidsharp7)
    *Adds a new column called "tags" to the dataset column view along with the tags associated with the dataset column.*
* Web: add HTML Tool Tip [`#2601`](https://github.com/MarquezProject/marquez/pull/2601) [@davidsharp7](https://github.com/davidsharp7)
    *Adds a Tool Tip to display basic node details.*

### Fixed

* Web: fix dataset saga for paging [`#2615`](https://github.com/MarquezProject/marquez/pull/2615) [@phixme](https://github.com/phixMe)
    *Updates the saga, changes the default page size.*
* API: perf/improve `jobdao` query [`#2609`](https://github.com/MarquezProject/marquez/pull/2609) [@algorithmy1](https://github.com/algorithmy1)
    *Optimizes the query to make use of Common Table Expressions to fetch the required data more efficiently and before the join, fixing a significant bottleneck.*

### Changed

* Docker: Postgres `14` [`#2607`](https://github.com/MarquezProject/marquez/pull/2607) [@wslulciuc](https://github.com/wslulciuc)
    *Bumps the recommended version of Postgres to 14.*
    *When deploying locally, you might need to run `./docker/down.sh` to clean existing volumes.*

### Removed

* Client: tolerate null transformation attrs in field model [`#2600`](https://github.com/MarquezProject/marquez/pull/2600) [@davidjgoss](https://github.com/davidjgoss)
    *Removes the @NonNull annotation from the client class and the @NotNull from the model class.*

## [0.40.0](https://github.com/MarquezProject/marquez/compare/0.39.0...0.40.0) - 2023-08-15

### Added

* API: lineage events paging update [`#2577`](https://github.com/MarquezProject/marquez/pull/2577) [@phixme](https://github.com/phixMe)
    *Updates the API for lineage events and restyles the lineage events page to fix a number of bugs and code duplication.*
* Chart: do not use hardcoded Postgres image for init container [`#2579`](https://github.com/MarquezProject/marquez/pull/2579) [@terrpan](https://github.com/terrpan)
    *Adds a template in `chart/templates/helpers` to use the `global.imageRegistry` input value for the `wait-for-db` container to improve performance on private registries.*
* Web: add copy button for lineage ID [`#2578`](https://github.com/MarquezProject/marquez/pull/2578) [@AmandaYao00](https://github.com/AmandaYao00)
    *Adds a copy button to the IDs on the Events page.*

### Fixed

* API: add defaults for `idFromValue()` and `idFromValueAndType()` [`#2581`](https://github.com/MarquezProject/marquez/pull/2581) [@wslulciuc](https://github.com/wslulciuc)
    *Replaces the `null` values in these functions in `EventTypeResolver` with defaults.*
* Client: correct example syntax [`#2575`](https://github.com/MarquezProject/marquez/pull/2575) [@davidjgoss](https://github.com/davidjgoss)
    *Removes errant parens from the sample code's client instantiation.*

## [0.39.0](https://github.com/MarquezProject/marquez/compare/0.38.0...0.39.0) - 2023-08-08

### Added

* Web: add full graph toggle [`#2569`](https://github.com/MarquezProject/marquez/pull/2569) [@jlukenoff](https://github.com/jlukenoff)
    *Adds a toggle to the Lineage UI to let users switch between viewing the full graph and only the selected paths.*
* Web: add ARIA labels to input fields [`#2562`](https://github.com/MarquezProject/marquez/pull/2562) [@merobi-hub](https://github.com/merobi-hub)
    *Adds i18next-compliant ARIA labels to input fields for improved accessibility.*

### Changed
* Web: upgrade React to version 18 [`#2563`](https://github.com/MarquezProject/marquez/pull/2563) [@Xavier-Cliquennois](https://github.com/Xavier-Cliquennois)
    *Upgrades the Web client in order to utilize the latest version of Node.js and update all dependencies to their respective latest versions.*

### Fixed

* Web: fix the stylesheet for the date selector [`#2573`](https://github.com/MarquezProject/marquez/pull/2573) [@phixme](https://github.com/phixMe)
    *Fixes margins and moves the label to be more inline with what the defaults are to fix issues caused by the recent Material-UI upgrade.*
* Web: update i18n for general search filter and `runInfo` facets search [`#2557`](https://github.com/MarquezProject/marquez/pull/2557) [@merobi-hub](https://github.com/merobi-hub)
    *Adds missing i18n support for `runInfo` and search.*
* Docker: update web proxy import [`#2571`](https://github.com/MarquezProject/marquez/pull/2571) [@phixme](https://github.com/phixMe)
    *Updates the import style for the `http-proxy-middleware`.*

## [0.38.0](https://github.com/MarquezProject/marquez/compare/0.37.0...0.38.0) - 2023-08-02

### Added

* API: add db retention support [`#2486`](https://github.com/MarquezProject/marquez/pull/2486) [@wslulciuc](https://github.com/wslulciuc)
    *Adds migration, a `dbRetention` config in `marquez.yml` for enabling a retention policy, and a `db-retention` command for executing a policy.*
* API: add runs state indices [`#2535`](https://github.com/MarquezProject/marquez/pull/2535) [@phixme](https://github.com/phixMe)
    *Adds four indices to help run retention faster.*
* API: define `DbRetentionJob(Jdbi, DbRetentionConfig)` [`#2549`](https://github.com/MarquezProject/marquez/pull/2549) [@wslulciuc](https://github.com/wslulciuc)
    *Adds `@Positive` to `DbRetentionConfig` instance variables for validating `DbRetentionConfig` properties internally within the class.*
* API: add log for when retention job starts [`#2551`](https://github.com/MarquezProject/marquez/pull/2551) [@wslulciuc](https://github.com/wslulciuc)
    *Adds logging of `DbRetentionJob`.*

### Fixed

* API: fix slow dataset query updates [`#2534`](https://github.com/MarquezProject/marquez/pull/2534) [@phixme](https://github.com/phixMe)
    *Scopes down nested facet queries to be the same scope as the outer query.*
* Client/Python: increase namespace length to 1024 characters [`#2554`](https://github.com/MarquezProject/marquez/pull/2554) [@hloomupgrade](https://github.com/hloombaupgrade)
    *Changes the namespace length constraint to sync up with the Java client's.*
* Web: remove pagination in case of no content [`#2559`](https://github.com/MarquezProject/marquez/pull/2559) [@Nisarg-Chokshi](https://github.com/Nisarg-Chokshi)
    *Updates `Dataset` & `Event` route rendering to remove pagination in the case of no content.*


## [0.37.0](https://github.com/MarquezProject/marquez/compare/0.36.0...0.37.0) - 2023-07-17

### Added

* API: add ability to decode static metadata events [`#2495`](https://github.com/MarquezProject/marquez/pull/2495) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Introduces an `EventTypeResolver` for using the `schemaURL` field to decode `POST` requests to `/lineage` with `LineageEvent`s, `DatasetEvent`s or `JobEvent`s, as the first step in implementing static lineage support.*

### Fixed

* API: remove unnecessary DB updates [`#2531`](https://github.com/MarquezProject/marquez/pull/2531) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Prevent updates that are not needed and are deadlock-prone.*
* Web: revert URL encoding when fetching lineage [`#2529`](https://github.com/MarquezProject/marquez/pull/2529) [@jlukenoff](https://github.com/jlukenoff)
    *Reverts the node ID from being URL-encoded and allows the backend to return lineage details successfully even when a node ID contains special characters.*

## [0.36.0](https://github.com/MarquezProject/marquez/compare/0.35.0...0.36.0) - 2023-06-27

### Added

* UI: add an option for configuring the depth of the lineage graph [`#2525`](https://github.com/MarquezProject/marquez/pull/2525) [@jlukenoff](https://github.com/jlukenoff)
    *Makes the lineage UI a bit easier to navigate, especially for larger lineage graphs.*

### Fixed

* Docker: generate new `uuid` for `etl_menus` in seed data [`#2519`](https://github.com/MarquezProject/marquez/pull/2519) [@wslulciuc](https://github.com/wslulciuc)
    *Fixes a `runID` collision creating an invalid lineage graph when the seed command is used.*
* Docker: remove unnecessary copy command from Dockerfile [`#2516`](https://github.com/MarquezProject/marquez/pull/2516) [@Nisarg-Chokshi](https://github.com/MarquezProject/marquez/pull/2516)
    *Deletes redundant copy command.*
* Chart: enable RFC7230_LEGACY http compliance on application connectors by default [`#2524`](https://github.com/MarquezProject/marquez/pull/2524) [@jlukenoff](https://github.com/jlukenoff)
    *Adds this configuration to the helm chart by default to fix basic chart installation and ensure that the fix in [`#1419`](https://github.com/MarquezProject/marquez/pull/1419) does not revert.*


## [0.35.0](https://github.com/MarquezProject/marquez/compare/0.34.0...0.35.0) - 2023-06-13

### Added

* Web: add pagination to datasets [`#2512`](https://github.com/MarquezProject/marquez/pull/2512) [@merobi-hub](https://github.com/merobi-hub)
    *Adds pagination to the datasets route using the same approach employed for events.*
* Ability to decode static metadata events [`#2495`](https://github.com/MarquezProject/marquez/pull/2495) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
  *Adds the ability to distinguish on a bakend static metadata events introduced based on the [proposal](https://github.com/OpenLineage/OpenLineage/blob/main/proposals/1837/static_lineage.md).*

### Fixed

* Web: handle lineage graph cycles on the client [`#2506`](https://github.com/MarquezProject/marquez/pull/2506) [@jlukenoff](https://github.com/jlukenoff)
    *Fixes a bug where we blow the stack on the client-side if the user selects a node that is part of a cycle in the graph.*

## [0.34.0](https://github.com/MarquezProject/marquez/compare/0.33.0...0.34.0) - 2023-05-18

### Fixed

* Chart: skip regex after postgresql in chart/values.yaml [`#2488`](https://github.com/MarquezProject/marquez/pull/2488) [@wslulciuc](https://github.com/wslulciuc)
    *Fixes regex for version bump of chart/values.yaml in new-version.sh.*

## [0.33.0](https://github.com/MarquezProject/marquez/compare/0.32.0...0.33.0) - 2023-04-19

### Added

* API: support `inputFacets` and `outputFacets` from Openlineage specification [`#2417`](https://github.com/MarquezProject/marquez/pull/2417) [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Adds the ability to store `inputFacets` / `outputFacets` sent within datasets, exposing them through the Marquez API as part of the `Run` resource.*

### Fixed

* API: fix job update SQL to correctly use `simple_name` for job updates [`#2457`](https://github.com/MarquezProject/marquez/pull/2457) [@collado-mike](https://github.com/collado-mike)
    *Fixes a bug in the job update logic stemming from use of the FQN rather than the `simple_name` and updates the relevant test.*
* API: update SQL in backfill script for facet tables to improve performance [`#2461`](https://github.com/MarquezProject/marquez/pull/2461) [@collado-mike](https://github.com/collado-mike)
    *Dramatically improves migration performance by making the backfill script fetch events by `run_uuid` via a new temp table for tracking and sorting runs.*
* API: update v61 migration to handle duplicate job names before unique constraint [`#2464`](https://github.com/MarquezProject/marquez/pull/2464) [@collado-mike](https://github.com/collado-mike)
    *To fix a bug in the case of duplicate job FQNs, this renames jobs that have been symlinked to point to newer versions of themselves so that the job FQN doesn't conflict and the unique constraint (without regard to parent job) can be applied. Note: Any installations that have already applied this migration will not see any new operations on their data, but installations that have duplicates will need this fix for the migration to complete successfully.*
* API: make improvements to lineage query performance [`#2472`](https://github.com/MarquezProject/marquez/pull/2472) [@collado-mike](https://github.com/collado-mike)
    *Dramatically lessens the lineage query performance regression caused by removal of the `jobs_fqn` table in [`#2448`](https://github.com/MarquezProject/marquez/pull/2448).*
* UI: change color for selected node and edges on graph [`#2458`](https://github.com/MarquezProject/marquez/pull/2458) [@tito12](https://github.com/tito12)
    *Improves the visibility of the selected node and edges by increasing the contrast with the background.*
* UI: handle null `run.jobVersion` in `DatasetInfo.tsx` to fix rendering issues [`#2471`](https://github.com/MarquezProject/marquez/pull/2471) [@perttus](https://github.com/perttus)
    *Fixes an issue causing the UI to fail to render `DatasetInfo`.*
* UI: better handling of null `latestRun` for Jobs page [`#2467`](https://github.com/MarquezProject/marquez/pull/2467) [@perttus](https://github.com/perttus)
    *Fixes a bug causing the Jobs view to fail when `latestRun` is null.*

## [0.32.0](https://github.com/MarquezProject/marquez/compare/0.31.0...0.32.0) - 2023-03-20

### Fixed

* API: improve dataset facets access [`#2407`](https://github.com/MarquezProject/marquez/pull/2407) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Improves database query performance when accessing dataset facets by rewriting SQL queries in `DatasetDao` and `DatasetVersionDao`.*
* Chart: fix communication between the UI and the API [`#2430`](https://github.com/MarquezProject/marquez/pull/2430) [@thomas-delrue](https://github.com/thomas-delrue)
    *Defines the value for `MARQUEZ_PORT` as .Values.marquez.port (80) in the Helm Chart so the Marquez Web component can communicate with the API.*
* UI: always render `MqCode` [`#2454`](https://github.com/MarquezProject/marquez/pull/2454) [@JDarDagran](https://github.com/JDarDagran)
    *Fixes rendering of `DatasetInfo` and `RunInfo` pages when no `SqlJobFacet` exists.*

### Removed

* API: remove job context [`#2373`](https://github.com/MarquezProject/marquez/pull/2373) [@JDarDagran](https://github.com/JDarDagran)
    *Removes the use of job context and adds two endpoints for job/run facets per run. These are called from web components to replace the job context with `SQLJobFacet`.*
* API: remove `jobs_fqn` table and move FQN into jobs directly [`#2448`](https://github.com/MarquezProject/marquez/pull/2448) [@collado-mike](https://github.com/collado-mike)
    *Fixes loading of certain jobs caused by the inability to enforce uniqueness constraints on fully qualified job names.*

## [0.31.0](https://github.com/MarquezProject/marquez/compare/0.30.0...0.31.0) - 2023-02-16

### Added

* UI: add facet view enhancements [`#2336`](https://github.com/MarquezProject/marquez/pull/2336) [@tito12](https://github.com/tito12)
    *Creates a dynamic component offering the ability to navigate and search the JSON, expand sections and click on links.*
* UI: highlight selected path on graph and display status of jobs and datasets based on last 14 runs or latest quality facets [`#2384`](https://github.com/MarquezProject/marquez/pull/2384) [@tito12](https://github.com/tito12)
    *Adds highlighting of the visual graph based on upstream and downstream dependencies of selected nodes, makes displayed status reflect last 14 runs the case of jobs and latest quality facets in the case of datasets.*
* UI: enable auto-accessibility feature on graph nodes [`#2388`](https://github.com/MarquezProject/marquez/pull/2400) [@merobi-hub](https://github.com/merobi-hub)
    *Adds attributes to the `FontAwesomeIcon`s to enable a built-in accessibility feature.*

### Fixed

* API: add index to `jobs_fqn` table using `namespace_name` and `job_fqn` columns [`#2357`](https://github.com/MarquezProject/marquez/pull/2357) [@collado-mike](https://github.com/collado-mike)
    *Optimizes read queries by adding an index to this table.*
* API: add missing indices to `column_lineage`, `dataset_facets`, `job_facets` tables [`#2419`](https://github.com/MarquezProject/marquez/pull/2419) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Creates missing indices on reference columns in a number of database tables.*
* Spec: make data version and dataset types the same [`#2400`](https://github.com/MarquezProject/marquez/pull/2400) [@phixme](https://github.com/phixMe)
    *Makes the `fields` property the same for datasets and dataset versions, allowing type-generating systems to treat them the same way.*
* UI: show location button only when link to code exists [`#2409`](https://github.com/MarquezProject/marquez/pull/2409) [@tito12](https://github.com/tito12)
    *Makes the button visible only if the link is not empty.*

## [0.30.0](https://github.com/MarquezProject/marquez/compare/0.29.0...0.30.0) - 2023-01-31

### Added

* Proposals: add proposal for OL facet tables [`#2076`](https://github.com/MarquezProject/marquez/pull/2076) [@wslulciuc](https://github.com/wslulciuc)
    *Adds the proposal `Optimize query performance for OpenLineage facets`.*
* UI: display column lineage of a dataset [`#2293`](https://github.com/MarquezProject/marquez/pull/2293) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Adds a JSON preview of column-level lineage of a selected dataset to the UI.*
* UI: Add soft delete option to UI [`#2343`](https://github.com/MarquezProject/marquez/pull/2343) [@tito12](https://github.com/tito12)
    *Adds option to soft delete a data record with a dialog component and double confirmation.*
* API: split `lineage_events` table to `dataset_facets`, `run_facets`, and `job_facets` tables. [`#2350`](https://github.com/MarquezProject/marquez/pull/2350), [`2355`](https://github.com/MarquezProject/marquez/pull/2355), [`2359`](https://github.com/MarquezProject/marquez/pull/2359)
  [@wslulciuc](https://github.com/wslulciuc,), [@pawel-big-lebowski]( https://github.com/pawel-big-lebowski)
    *Performance improvement storing and querying facets.*
    *Migration procedure requires manual steps if database has more than 100K lineage events.*
    *We highly encourage users to review our [migration plan](https://github.com/MarquezProject/marquez/blob/main/api/src/main/resources/marquez/db/migration/V57__readme.md).*
* Docker: add new script for stopping Docker [`#2380`](https://github.com/MarquezProject/marquez/pull/2380) [@rossturk](https://github.com/rossturk)
    *Provides a clean way to stop a deployment via `docker-compose down`.*
* Docker: seed data for column lineage [`#2381`](https://github.com/MarquezProject/marquez/pull/2381) [@rossturk](https://github.com/rossturk)
    *Adds some `ColumnLineageDatasetFacet` JSON snippets to `docker/metadata.json` to seed data for column-level lineage facets.*

### Fixed

* API: validate `RunLink` and `JobLink` [`#2342`](https://github.com/MarquezProject/marquez/pull/2342) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Fixes validation of the `ParentRunFacet` to avoid `NullPointerException`s in the case of empty run sections.*
* Docker: use `docker-compose.web.yml` as base compose file [`#2360`](https://github.com/MarquezProject/marquez/pull/2360) [@wslulciuc](https://github.com/wslulciuc)
    *Fixes the Marquez HTTP server set in `docker/up.sh` so the script uses `docker-compose.web.yml` with overrides for `dev` set via `docker-compose.web-dev.yml`.*
* Docs: update copyright headers [`#2353`](https://github.com/MarquezProject/marquez/pull/2353) [@merobi-hub](https://github.com/merobi-hub)
    *Updates the headers with the current year.*
* Chart: fix Helm chart [`#2374`](https://github.com/MarquezProject/marquez/pull/2374) [@perttus](https://github.com/perttus)
    *Fixes minor issues with the Helm chart.*
* Spec: update dataset version API spec [`#2389`](https://github.com/MarquezProject/marquez/pull/2389) [@phixme](https://github.com/phixMe)
    *Adds `limit` and `offset` to the openapi.yml spec file as query parameters.*

## [0.29.0](https://github.com/MarquezProject/marquez/compare/0.28.0...0.29.0) - 2022-12-19

### Added

* Column-lineage endpoints supports point-in-time requests [`#2265`](https://github.com/MarquezProject/marquez/pull/2265) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Enable requesting `column-lineage` endpoint by a dataset version, job version or dataset field of a specific dataset version.*
* Present column lineage of a dataset [`#2293`](https://github.com/MarquezProject/marquez/pull/2293) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Column lineage of a dataset with a single level of depth can be displayed in datase details tab.*
* Add point-in-time requests support to column-lineage endpoints [`#2265`](https://github.com/MarquezProject/marquez/pull/2265) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Enables requesting `column-lineage` endpoint by a dataset version, job version or dataset field of a specific dataset version.*
* Add column lineage point-in-time Java client methods [`#2269`](https://github.com/MarquezProject/marquez/pull/2269) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Java client methods to retrieve point-in-time `column-lineage`. Please note that the existing methods `getColumnLineageByDataset`, `getColumnLineageByDataset` and `getColumnLineageByDatasetField` are replaced by a single `getColumnLineage` method taking `NodeId` as a parameter.*
* Add raw event viewer to UI [`#2249`](https://github.com/MarquezProject/marquez/pull/2249) [@tito12](https://github.com/tito12)
    *A new events page enables filtering events by date and expanding the payload by clicking on each event.*
* Update events page with styling synchronization [`#2324`](https://github.com/MarquezProject/marquez/pull/2324) [@phixMe](https://github.com/phixMe)
    *Makes some updates to the new page to make it conform better to the overall design system.*
* Update helm Ingress template to be cross-compatible with recent k8s versions [`#2275`](https://github.com/MarquezProject/marquez/pull/2275) [@jlukenoff](https://github.com/jlukenoff)
    *Certain components of the Ingress schema have changed in recent versions of Kubernetes. This change updates the Ingress helm template to render based on the semantic Kubernetes version.*
* Add delete namespace endpoint doc to OpenAPI docs [`#2295`](https://github.com/MarquezProject/marquez/pull/2295) [@mobuchowski](https://github.com/mobuchowski)
    *Adds a doc about the delete namespace endpoint.*
* Add i18next and language switcher for i18n of UI [`#2254`](https://github.com/MarquezProject/marquez/pull/2254) [@merobi-hub](https://github.com/merobi-hub) [@phixMe](https://github.com/phixMe)
    *Adds i18next framework, language switcher, and translations for i18n of UI.*
* Add indexed `created_at` column to lineage events table [`#2299`](https://github.com/MarquezProject/marquez/pull/2299) [@prachim-collab](https://github.com/prachim-collab)
    *A new timestamp column in the database supports analytics use cases by allowing for identification of incrementally created events (backwards-compatible).*

### Fixed

* Allow null column type in column lineage [`#2272`](https://github.com/MarquezProject/marquez/pull/2272) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *The column-lineage endpoint was throwing an exception when no data type of the field was provided. Includes a test.*
* Include error message for JSON processing exception [`#2271`](https://github.com/MarquezProject/marquez/pull/2271) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *In case of JSON processing exceptions, the Marquez API now returns an exception message to a client.*
* Fix column lineage when multiple jobs write to same dataset [`#2289`](https://github.com/MarquezProject/marquez/pull/2289) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *The fix deprecates the way the fields `transformationDescription` and `transformationType` are returned. The deprecated way of returning those fields will be removed in 0.30.0.*
* Use raw link for `iconSearchArrow.svg` [`#2280`](https://github.com/MarquezProject/marquez/pull/2280) [@wslulciuc](https://github.com/wslulciuc)
    *Using a direct link to the events viewer icon fixes a loading issue.*
* Fill run state of parent run when created by child run [`#2296`](https://github.com/MarquezProject/marquez/pull/2296) [@fm100](https://github.com/fm100)
    *Adds a run state to the parent at creation time to address a missing run state issue in Airflow integration.*
* Update migration query to make it work with existing view [`#2308`](https://github.com/MarquezProject/marquez/pull/2308) [@fm100](https://github.com/fm100)
    *Changes the V52 migration query to drop the view before `ALTER`. Because repeatable migration runs only when its checksum changes, it was necessary to get the view definition first then drop and recreate it.*
* Fix lineage for orphaned datasets [`#2314`](https://github.com/MarquezProject/marquez/pull/2314) [@collado-mike](https://github.com/collado-mike)
    *Fixes lineage for datasets generated by jobs whose current versions no longer write to the databases in question.*
* Ensure job data in lineage query is not null or empty [`#2253`](https://github.com/MarquezProject/marquez/pull/2253) [@wslulciuc](https://github.com/wslulciuc)
    *Changes the API to return an empty graph in the edge case of a job UUID that has no lineage when calling `LineageDao.getLineage()` yet is associated with a dataset. This case formerly resulted in an empty set and backend exception. Also includes logging and an API check for a `nodeID`.*
* Make `name` and `type` required for datasets [`#2305`](https://github.com/MarquezProject/marquez/pull/2305) [@wslulciuc](https://github.com/wslulciuc)
    *When generating Typescript from the OpenAPI spec, `name` and `type` were not required but should have been.*
* Remove unused filter on `RunDao.updateStartState()` [`#2319`](https://github.com/MarquezProject/marquez/pull/2319) [@wslulciuc](https://github.com/wslulciuc)
    *Removes the conditions `updated_at < transitionedAt` and `start_run_state_uuid != null` to allow for updating the run state.*
* Update linter [`#2322`](https://github.com/MarquezProject/marquez/pull/2322) [@phixMe](https://github.com/phixMe)
    *Adds `npm run eslint-fix` to the CI config to fail if it does not return with a RC 0.*
* Fix asset loading for web [`#2323`](https://github.com/MarquezProject/marquez/pull/2323) [@phixMe](https://github.com/phixMe)
    *Fixes the webpack config and allows files to be imported in a modern capacity that enforces the assets exist.*

## [0.28.0](https://github.com/MarquezProject/marquez/compare/0.27.0...0.28.0) - 2022-11-21

### Added

* Optimize current runs query for lineage API [`#2211`](https://github.com/MarquezProject/marquez/pull/2211) [@prachim-collab](https://github.com/prachim-collab)
    *Add a simpler, alternate `getCurrentRuns` query that gets only simple runs from the database without the additional data from tables such as `run_args`, `job_context`, `facets`, etc., which required extra table joins.*
* Add Code Quality, DCO and Governance docs to project [`#2237`](https://github.com/MarquezProject/marquez/pull/2237) [`#2241`](https://github.com/MarquezProject/marquez/pull/2241) [@merobi-hub](https://github.com/merobi-hub)
    *Adds a number of standard governance and procedure docs to the project.*
* Add possibility to soft-delete namespaces [`#2244`](https://github.com/MarquezProject/marquez/pull/2244) [@mobuchowski](https://github.com/mobuchowski)
    *Adds the ability to "hide" inactive namespaces. The namespaces are undeleted when a relevant OL event is received.*
* Add search service proposal [`#2203`](https://github.com/MarquezProject/marquez/pull/2203) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Proposes using ElasticSearch as a pluggable search service to enhance the search feature in Marquez and adding the ability to turn it off, as well. Includes ideas about what should be indexed and the requirements for the interface.*

### Fixed

* Show facets even when dataset has no fields [`#2214`](https://github.com/MarquezProject/marquez/pull/2214) [@JDarDagran](https://github.com/JDarDagran)
    *Changes the logic in the `DatasetInfo` component to always show facets so that dataset facets are visible in the UI even if no dataset fields have been set.*
* Appreciate column prefix when given for `ended_at` [`#2231`](https://github.com/MarquezProject/marquez/pull/2231) [@fm100](https://github.com/fm100)
    *The `ended_at` column was always null when querying if `columnPrefix` was given for the mapper. Now, `columnPrefix` is included when checking for column existence.*
* Fix bug keeping jobs from being properly deleted [`#2244`](https://github.com/MarquezProject/marquez/pull/2244) [@mobuchowski](https://github.com/mobuchowski)
    *It wasn't possible to delete jobs created from events that had a `ParentRunFacet`. Now it's possible.*
* Fix symlink table column length ['#2217'](https://github.com/MarquezProject/marquez/pull/2217) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *The dataset's name column in the `dataset_symlinks` table was shorter than the column in the datasets table. Changes the existing V48 migration script to allow proper migration for users who did not upgrade yet, and adds an extra migration script to extend the column length for users who did upgrade but did not experience the issues.*


## [0.27.0](https://github.com/MarquezProject/marquez/compare/0.26.0...0.27.0) - 2022-10-24

### Added

* Implement dataset symlink feature [`#2066`](https://github.com/MarquezProject/marquez/pull/2066) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Adds support for multiple dataset names and adds edges to the lineage graph based on symlinks.*
* Store column lineage facets in separate table [`#2096`](https://github.com/MarquezProject/marquez/pull/2096) [@mzareba382](https://github.com/mzareba382) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Adds a column-level lineage representation and API endpoint to retrieve column-level lineage data from the Marquez database.*
* Add a lineage graph endpoint for column lineage [`#2124`](https://github.com/MarquezProject/marquez/pull/2124) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Allows for the storing of column-lineage information from events in the Marquez database and exposes column lineage through a graph endpoint.*
* Enrich returned dataset resource with column lineage information [`#2113`](https://github.com/MarquezProject/marquez/pull/2113) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Extends the `/api/v1/namespaces/{namespace}/datasets` endpoint to return the `columnLineage` facet.*
* Add downstream column lineage [`#2159`](https://github.com/MarquezProject/marquez/pull/2159) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Extends the recursive query that returns column lineage nodes to traverse the graph for downstream nodes.*
* Implement column lineage within Marquez Java client  [`#2163`](https://github.com/MarquezProject/marquez/pull/2163) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Adds Marquez API client methods for column lineage.*
* Provide `dataset_symlinks` table for `SymlinkDatasetFacet` [`#2087`](https://github.com/MarquezProject/marquez/pull/2087) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Modifies Marquez to handle the new `SymlinkDatasetFacet` in the OpenLineage spec.*
* Display current run state for job node in lineage graph [`#2146`](https://github.com/MarquezProject/marquez/pull/2146) [@wslulciuc](https://github.com/wslulciuc)
    *Fills job nodes in the lineage graph with the latest run state and makes some minor changes to column names used to display dataset and job metadata.*
* Include column lineage in dataset resource [`#2148`](https://github.com/MarquezProject/marquez/pull/2148) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Creates a method in `ColumnLineageService` to enrich `Dataset` with column lineage information and uses the method in `DatasetResource`.*
* Add indices on the job table [`#2161`](https://github.com/MarquezProject/marquez/pull/2161) [@phixMe](https://github.com/phixMe)
    *Adds indices to the fields used we join on inside the lineage query to speed up the join operation in the `/lineage` query.*
* Add endpoint to get column lineage by a job [`#2204`](https://github.com/MarquezProject/marquez/pull/2204) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Changes the API to make column lineage available for jobs.*
* Add column lineage methods to Python client [`#2209`](https://github.com/MarquezProject/marquez/pull/2209) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Implements methods for column lineage in the Python client.*

### Changed

* Update insert job function to avoid joining on symlinks for jobs with no symlinks [`#2144`](https://github.com/MarquezProject/marquez/pull/2144) [@collado-mike](https://github.com/collado-mike)
    *Radically reduces the database compute load in Marquez installations that frequently create a large number of new jobs.*
* Increase size of `column-lineage.description` column [`#2205`](https://github.com/MarquezProject/marquez/pull/2205) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *`VARCHAR(255)` was too small for some users.*

### Fixed

* Add support for `parentRun` facet as reported by older Airflow OpenLineage versions [`#2130`](https://github.com/MarquezProject/marquez/pull/2130) [@collado-mike](https://github.com/collado-mike)
    *Adds a `parentRun` alias to the `LineageEvent` `RunFacet`.*
* Add fix and tests for handling Airflow DAGs with dots and task groups [`#2126`](https://github.com/MarquezProject/marquez/pull/2126) [@collado-mike](https://github.com/collado-mike) [@wslulciuc](https://github.com/wslulciuc)
    *Fixes a recent change that broke how Marquez handles DAGs with dots and tasks within task groups and adds test cases to validate.*
* Fix version bump in `docker/up.sh` [`#2129`](https://github.com/MarquezProject/marquez/pull/2129) [@wslulciuc](https://github.com/wslulciuc)
    *Defines a `VERSION` variable to bump on a release.*
* Use `clean` when running `shadowJar` in Dockerfile [`#2145`](https://github.com/MarquezProject/marquez/pull/2145) [@wslulciuc](https://github.com/wslulciuc)
    *Ensures the directory `api/build/libs/` is cleaned before building the JAR again and updates `.dockerignore` to ignore `api/build/*`.*
* Fix bug that caused a single run event to create multiple jobs [`#2162`](https://github.com/MarquezProject/marquez/pull/2162) [@collado-mike](https://github.com/collado-mike)
    *Checks to see if a run with the given ID already exists and uses the pre-associated job if so.*
* Fix column lineage returning multiple entries for job run multiple times [`#2176`](https://github.com/MarquezProject/marquez/pull/2176) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Makes column lineage return a column dependency only once if a job has been run several times.*
* Fix API spec issues [`#2178`](https://github.com/MarquezProject/marquez/pull/2178) [@phixMe](https://github.com/phixMe)
    *Fixes issues with type generators in the `putDataset` API.*
* Fix downstream recursion [`#2181`](https://github.com/MarquezProject/marquez/pull/2181) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Fixes issue causing same node to be added to recursive table multiple times.*
* Update `jobs_current_version_uuid_index` and `jobs_symlink_target_uuid_index` to ignore `NULL` values [`#2186`](https://github.com/MarquezProject/marquez/pull/2186) [@collado-mike](https://github.com/collado-mike)
    *Avoids writing to the indices when the indexed values added by [#2161](https://github.com/MarquezProject/marquez/pull/2161) are null.*

## [0.26.0](https://github.com/MarquezProject/marquez/compare/0.25.0...0.26.0) - 2022-09-15

### Added

* Update FlywayFactory to support an argument to customize the schema programatically [`#2055`](https://github.com/MarquezProject/marquez/pull/2055) [@collado-mike](https://github.com/collado-mike)
    *Note: this change does not aim to support custom schemas from configuration.*
* Add steps on proposing changes to Marquez [`#2065`](https://github.com/MarquezProject/marquez/pull/2065) [@wslulciuc](https://github.com/wslulciuc)
    *Adds steps on how to submit a proposal for review along with a design doc template.*
* Add `--metadata` option to seed backend with ol events [`#2082`](https://github.com/MarquezProject/marquez/pull/2082) [@wslulciuc](https://github.com/wslulciuc)
    *Updates the `seed` command to load metadata from a file containing an array of OpenLineage events via the `--metadata` option. (Metadata used in the command was not being defined using the OpenLineage standard.)*
* Improve documentation on `nodeId` in the spec [`#2084`](https://github.com/MarquezProject/marquez/pull/2084) [@howardyoo](https://github.com/howardyoo)
    *Adds complete examples of `nodeId` to the spec.*
* Add `metadata` cmd [`#2091`](https://github.com/MarquezProject/marquez/pull/2091) [@wslulciuc](https://github.com/wslulciuc)
    *Adds cmd `metadata` to generate OpenLineage events; generated events will be saved to a file called `metadata.json` that can be used to seed Marquez via the `seed` cmd. (We lacked a way to performance test the data model of Marquez with significantly large OL events.)*
* Add possibility to soft-delete datasets and jobs [`#2032`](https://github.com/MarquezProject/marquez/pull/2032) [`#2099`](https://github.com/MarquezProject/marquez/pull/2099) [`#2101`](https://github.com/MarquezProject/marquez/pull/2101) [@mobuchowski](https://github.com/mobuchowski)
    *Adds the ability to "hide" inactive datasets and jobs through the UI. (This PR does not include the UI part.) The feature works by adding an `is_hidden` flag to both datasets and jobs tables. Then, it changes `jobs_view` and adds `datasets_view`, which hides rows where the `is_hidden` flag is set to True. This makes writing proper queries easier since there is no need to do this filtering manually. The soft-delete is reversed if the job or dataset is updated again because the new version reverts the flag.*
* Add raw OpenLineage events API [`#2070`](https://github.com/MarquezProject/marquez/pull/2070) [@mobuchowski](https://github.com/mobuchowski)
    *Adds an API that returns raw OpenLineage events sorted by time and optionally filtered by namespace. Filtering by namespace takes into account both job and dataset namespaces*
* Create column lineage endpoint proposal [`#2077`](https://github.com/MarquezProject/marquez/pull/2077) [@julienledem](https://github.com/julienledem) [@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
    *Adds a proposal to implement a column-level lineage endpoint in Marquez to leverage the column-level lineage facet in OpenLineage.*

### Changed

* Update lineage query to only look at jobs with inputs or outputs [`#2068`](https://github.com/MarquezProject/marquez/pull/2068) [@collado-mike](https://github.com/collado-mike)
    *Changes the lineage query to query the `job_versions_io_mapping` table and INNER join with the `jobs_view` so that only jobs that have inputs or outputs are present in the `jobs_io` CTE. Hence, the table becomes very small and the recursive join in the lineage CTE very fast. (In many environments, a large number of jobs reporting events have no inputs or outputs - e.g., PythonOperators in an Airflow deployment. If a Marquez installation has many of these, the lineage query spends much of its time searching for overlaps with jobs that have no inputs or outputs.)*
* Persist OpenLineage event before updating Marquez model [`#2069`](https://github.com/MarquezProject/marquez/pull/2069) [@fm100](https://github.com/fm100)
    *Switches the order of the code in order to persist the OpenLineage event first and then update the Marquez model. (When the `RunTransitionListener` was invoked, the OpenLineage event was not persisted to the database. Because the OpenLineage event is the source of truth for all Marquez run transitions, it should be available from `RunTransitionListener`.)*
* Drop requirement to provide marquez.yml for `seed` cmd [`#2094`](https://github.com/MarquezProject/marquez/pull/2094) [@wslulciuc](https://github.com/wslulciuc)
    *Use `io.dropwizard.cli.Command` instead of `io.dropwizard.cli.ConfiguredCommand` to no longer require passing marquez.yml as an argument to the `seed` cmd. (The marquez.yml argument is not used in the `seed` cmd.)*

### Fixed

* Fix/rewrite jobs fqn locks [`#2067`](https://github.com/MarquezProject/marquez/pull/2067) [@collado-mike](https://github.com/collado-mike)
    *Updates the function to only update the table if the job is a new record or if the `symlink_target_uuid` is distinct from the previous value. (The `rewrite_jobs_fqn_table` function was inadvertently updating jobs even when no metadata about the job had changed. Under load, this caused significant locking issues, as the `jobs_fqn` table must be locked for every job update.)*
* Fix `enum` string types in the OpenAPI spec [`#2086`](https://github.com/MarquezProject/marquez/pull/2086) [@studiosciences](https://github.com/studiosciences)
    *Changes the type to `string`. (`type: enum` was not valid in OpenAPI spec.)*
* Fix incorrect PostgresSQL version [`#2089`](https://github.com/MarquezProject/marquez/pull/2089) [@jabbera](https://github.com/jabbera)
    *Corrects the tag for PostgresSQL.*
* Update `OpenLineageDao` to handle Airflow run UUID conflicts [`#2097`](https://github.com/MarquezProject/marquez/pull/2097) [@collado-mike](https://github.com/collado-mike)
    *Alleviates the problem for Airflow installations that will continue to publish events with the older OpenLineage library. This checks the namespace of the parent run and verifies that it matches the namespace in the `ParentRunFacet`. If not, it generates a new parent run ID that will be written with the correct namespace. (The Airflow integration was generating conflicting UUIDs based on the DAG name and the DagRun ID without accounting for different namespaces. In Marquez installations that have multiple Airflow deployments with duplicated DAG names, we generated jobs whose parents have the wrong namespace.)*

## [0.25.0](https://github.com/MarquezProject/marquez/compare/0.24.0...0.25.0) - 2022-08-08

### Fixed

* Fix py module release [`#2057`](https://github.com/MarquezProject/marquez/pull/2057) [@wslulciuc](https://github.com/wslulciuc)
* Use `/bin/sh` in `web/docker/entrypoint.sh` [`#2059`](https://github.com/MarquezProject/marquez/pull/2059) [@wslulciuc](https://github.com/wslulciuc)

## [0.24.0](https://github.com/MarquezProject/marquez/compare/0.23.0...0.24.0) - 2022-08-02

### Added

* Add copyright lines to all source files [`#1996`](https://github.com/MarquezProject/marquez/pull/1996) [@merobi-hub](https://github.com/MarquezProject/marquez/commits?author=merobi-hub)
* Add copyright and license guidelines in `CONTRIBUTING.md` [@wslulciuc](https://github.com/wslulciuc)
* Add `@FlywayTarget` annotation to migration tests to control flyway upgrades [`#2035`](https://github.com/MarquezProject/marquez/pull/2035) [@collado-mike](https://github.com/collado-mike)

### Changed

* Updated `jobs_view` to stop computing FQN on reads and to compute on _writes_ instead [`#2036`](https://github.com/MarquezProject/marquez/pull/2036) [@collado-mike](https://github.com/collado-mike)
* Runs row reduction [`#2041`](https://github.com/MarquezProject/marquez/pull/2041) [@collado-mike](https://github.com/collado-mike)

### Fixed

* Update `Run` in the openapi spec to include a `context` field [`#2020`](https://github.com/MarquezProject/marquez/pull/2020) [@esaych](https://github.com/Esaych)
* Fix dataset openapi model [`#2038`](https://github.com/MarquezProject/marquez/pull/2038) [@esaych](https://github.com/Esaych)
* Fix casing on `lastLifecycleState` [`#2039`](https://github.com/MarquezProject/marquez/pull/2039) [@esaych](https://github.com/Esaych)
* Fix V45 migration to include initial population of jobs_fqn table [`#2051`](https://github.com/MarquezProject/marquez/pull/2051) [@collado-mike](https://github.com/collado-mike)
* Fix symlinked jobs in queries [`#2053`](https://github.com/MarquezProject/marquez/pull/2053) [@collado-mike](https://github.com/collado-mike)

## [0.23.0](https://github.com/MarquezProject/marquez/compare/0.22.0...0.23.0) - 2022-06-16

### Added

* Update docker-compose.yml: Randomly map postgres db port [`#2000`](https://github.com/MarquezProject/marquez/pull/2000) [@RNHTTR](https://github.com/RNHTTR)
* Job parent hierarchy [`#1935`](https://github.com/MarquezProject/marquez/pull/1935) [`#1980`](https://github.com/MarquezProject/marquez/pull/1980) [`#1992`](https://github.com/MarquezProject/marquez/pull/1992) [@collado-mike](https://github.com/collado-mike)

### Changed

* Set default limit for listing datasets and jobs in UI from `2000` to `25` [`#2018`](https://github.com/MarquezProject/marquez/pull/2018) [@wslulciuc](https://github.com/wslulciuc)
* Update OpenLineage write API to be non-transactional and avoid unnecessary locks on records under heavy contention [@collado-mike](https://github.com/collado-mike)

### Fixed

* Return the tag for postgresql to 12.1.0 [`#2015`](https://github.com/MarquezProject/marquez/pull/2015) [@rossturk](https://github.com/rossturk)

## [0.22.0](https://github.com/MarquezProject/marquez/compare/0.21.0...0.22.0) - 2022-05-16

### Added

* Add support for `LifecycleStateChangeFacet` with an ability to softly delete datasets [`#1847`](https://github.com/MarquezProject/marquez/pull/1847)[@pawel-big-lebowski](https://github.com/pawel-big-lebowski)
* Enable pod specific annotations in Marquez Helm Chart via `marquez.podAnnotations` [`#1945`](https://github.com/MarquezProject/marquez/pull/1945) [@wslulciuc](https://github.com/wslulciuc)
* Add support for job renaming/redirection via symlink [`#1947`](https://github.com/MarquezProject/marquez/pull/1947) [@collado-mike](https://github.com/collado-mike)
* Add `Created by` view for dataset versions along with SQL syntax highlighting in web UI [`#1929`](https://github.com/MarquezProject/marquez/pull/1929) [@phixMe](https://github.com/phixMe)
* Add `operationId` to openapi spec [`#1978`](https://github.com/MarquezProject/marquez/pull/1978) [@phixMe](https://github.com/phixMe)

### Changed

* Upgrade Flyway to v7.6.0 [`#1974`](https://github.com/MarquezProject/marquez/pull/1974) [@dakshin-k](https://github.com/dakshin-k)

### Fixed

* Remove size limits on namespaces, dataset names, and and source connection urls [`#1925`](https://github.com/MarquezProject/marquez/pull/1925) [@collado-mike](https://github.com/collado-mike)
* Update namespace names to allow `=`, `@`, and `;` [`#1936`](https://github.com/MarquezProject/marquez/pull/1936) [@mobuchowski](https://github.com/mobuchowski)
* Time duration display in web UI [`#1950`](https://github.com/MarquezProject/marquez/pull/1950) [@phixMe](https://github.com/phixMe)
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

----
SPDX-License-Identifier: Apache-2.0
Copyright 2018-2023 contributors to the Marquez project.
