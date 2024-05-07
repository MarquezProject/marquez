# Proposal: Column lineage endpoint proposal

Author(s): @rnhttr, @wslulciuc, @collado-mike

Created: 20022-09-30

Discussion:
* [Proposal: Update DatasetVersion versioning #2071](https://github.com/MarquezProject/marquez/issues/2071)
* [Discrepancy in building DatasetVersionIds and which version is used #1977 ](https://github.com/MarquezProject/marquez/issues/1977)
* [Dataset currentVersion not in Dataset Versions Listing #1883](https://github.com/MarquezProject/marquez/issues/1883)

## Overview

The current versioning system leads to confusion (e.g. [#1883](https://github.com/MarquezProject/marquez/issues/1883)). DatasetVersion, for example, has a uuid field (of type UUID) and a version field (also of type UUID). In a practical sense, I think these fields are redundant.

Additionally, external data systems might already support dataset versioning (e.g. delta, iceberg). It'd make sense for Marquez to be able support these external versions.

## Proposal
This proposal aims to simplify the Marquez versioning system and enable new features to support dataset versions supplied by external data systems.

Moving forward, `DatasetVersionRow` and its cousin [`ExtendedDatasetVersionRow`](https://github.com/MarquezProject/marquez/blob/0.26.0/api/src/main/java/marquez/db/models/ExtendedDatasetVersionRow.java#L26) will no longer support a `version` field; rather, this will be dropped and replaced by a nullable `externalVersion`. This `externalVersion` field will be populated if provided, using [OpenLineage's DatasetVersionDatasetFacet facet](https://github.com/OpenLineage/OpenLineage/blob/main/spec/facets/DatasetVersionDatasetFacet.json) to support external dataset versions. `DatasetVersionRow`'s `uuid` field will serve as the version's sole identifier.

Similarly, `JobVersionRow` and its cousin [`ExtendedJobVersionRow`](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/ExtendedJobVersionRow.java#L32) will no longer support a `version` field, and `JobVersionRow`'s `uuid` field will serve as the version's sole identifier.

## Implementation

1. Update `Version.getValue()` to be of type String
2. Drop the `version` field from the following objects:
   1. [db.models.DatasetVersionRow's version field](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/DatasetVersionRow.java#L25)
   2. [db.models.ExtendedDatasetVersionRow](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/ExtendedDatasetVersionRow.java#L26)
   3. [api.models.JobVersion](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/api/models/JobVersion.java)
   4. [db.models.JobVersion](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/JobVersionRow.java#L32)
   5. [db.models.ExtendedJobVersonRow](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/ExtendedJobVersionRow.java#L32)
   6. [client.models.DatasetVersion](https://github.com/MarquezProject/marquez/blob/main/clients/java/src/main/java/marquez/client/models/DatasetVersion.java)
   7. [client.models.DbTableVersion](https://github.com/MarquezProject/marquez/blob/main/clients/java/src/main/java/marquez/client/models/DbTableVersion.java)
   8. [client.models.JobVersion](https://github.com/MarquezProject/marquez/blob/main/clients/java/src/main/java/marquez/client/models/JobVersion.java)
   9. [client.models.JobVersion](https://github.com/MarquezProject/marquez/blob/main/clients/java/src/main/java/marquez/client/models/JobVersionId.java)
   10. [client.models.StreamVersion](https://github.com/MarquezProject/marquez/blob/main/clients/java/src/main/java/marquez/client/models/StreamVersion.java)
3. Add a field `external_version` (String) to the following Objects:
   1. `DatasetVersioRow`
   2. `ExtendedDatasetVersionRow`
   3. `client.models.DatasetVersion`
   4. `client.models.DbTableVersion`
   5. `client.models.StreamVersion`
4. Use OpenLineage's DatasetVersionDatasetFacet facet to support external dataset versions.
5. Update queries for [`DatasetVersionDao`](https://github.com/MarquezProject/marquez/blob/0.26.0/api/src/main/java/marquez/db/DatasetVersionDao.java) and [`JobVersionDao`](https://github.com/MarquezProject/marquez/blob/0.26.0/api/src/main/java/marquez/db/JobVersionDao.java) to reference each version's `uuid` field instead of the current `version` field
6. Implement public API functionality (i.e. `GET`, `LIST`) for `externalVersion`
7. Database migration scripts to update client databases to support the new schema for `DatasetVersionRow`, `ExtendedDatasetVersionRow`, `JobVersionRow` and `ExtendedJobVersionRow`.
    1. These changes are backward compatible as the only changes are deleted fields and new optional fields.


## Next Steps

Review of this proposal and production of detailed design for the implementation.:
