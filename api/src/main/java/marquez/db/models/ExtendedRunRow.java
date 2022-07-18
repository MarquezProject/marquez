/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetVersionId;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExtendedRunRow extends RunRow {
  @Getter @NonNull private final List<DatasetVersionId> inputVersions;
  @Getter @NonNull private final List<DatasetVersionId> outputVersions;
  @Getter private final String namespaceName;
  @Getter private final String jobName;
  @Getter private final String args;

  public ExtendedRunRow(
      final UUID uuid,
      final Instant createdAt,
      final Instant updatedAt,
      final UUID jobUuid,
      @Nullable final UUID jobVersionUuid,
      @Nullable final UUID parentRunUuid,
      final UUID runArgsUuid,
      final List<DatasetVersionId> inputVersions,
      final List<DatasetVersionId> outputVersions,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @Nullable final String currentRunState,
      @Nullable final Instant startedAt,
      @Nullable final UUID startRunStateUuid,
      @Nullable final Instant endedAt,
      @Nullable final UUID endRunStateUuid,
      @NonNull final String args,
      @NonNull final String namespaceName,
      @NonNull final String jobName) {
    super(
        uuid,
        createdAt,
        updatedAt,
        jobUuid,
        jobVersionUuid,
        parentRunUuid,
        runArgsUuid,
        nominalStartTime,
        nominalEndTime,
        currentRunState,
        startedAt,
        startRunStateUuid,
        endedAt,
        endRunStateUuid);
    this.inputVersions = inputVersions;
    this.outputVersions = outputVersions;
    this.args = args;
    this.jobName = jobName;
    this.namespaceName = namespaceName;
  }

  public boolean hasInputVersionUuids() {
    return !inputVersions.isEmpty();
  }
}
