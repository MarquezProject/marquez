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

1. Update Version.getValue() to be of type String
2. Drop [DatasetVersionRow's version field](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/DatasetVersionRow.java#L25)
   1. Drop [ExtendedDatasetVersionRow's version field](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/ExtendedDatasetVersionRow.java#L26)
3. Add a field to DatasetVersioRow: external_version (String)
   1. Add a field to ExtendedDatasetVersionRow: external_version (String)
4. Drop [JobVersion's version field](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/JobVersionRow.java#L32)
   1. Drop [ExtendedJobVersonRow's version field](https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/db/models/ExtendedJobVersionRow.java#L32)
5. Use OpenLineage's DatasetVersionDatasetFacet facet to support external dataset versions.
6. Update queries in DAOs for [`DatasetVersion`](https://github.com/MarquezProject/marquez/blob/0.26.0/api/src/main/java/marquez/db/DatasetVersionDao.java) and [`JobVersion`](https://github.com/MarquezProject/marquez/blob/0.26.0/api/src/main/java/marquez/db/JobVersionDao.java) to reference each version's `uuid` field instead of the current `version` field.
7. Database migration scripts to update client databases to support the new schema for `DatasetVersionRow`, `ExtendedDatasetVersionRow`, `JobVersionRow` and `ExtendedJobVersionRow`.
    1. These changes are backward compatible as the only changes are deleted fields and new optional fields.


## Next Steps

Review of this proposal and production of detailed design for the implementation.:
