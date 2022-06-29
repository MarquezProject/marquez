/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExtendedJobVersionRow extends JobVersionRow {
  @Getter private final String context;
  @Getter private @NonNull String namespaceName;
  @Getter private @NonNull String name;

  public ExtendedJobVersionRow(
      final UUID uuid,
      final Instant createdAt,
      final Instant updatedAt,
      final UUID jobUuid,
      final UUID jobContextUuid,
      final List<UUID> inputUuids,
      final List<UUID> outputUuids,
      final String location,
      final UUID version,
      final UUID latestRunUuid,
      @NonNull final String context,
      @NonNull final String namespaceName,
      @NonNull final String name,
      @NonNull final UUID namespaceUuid) {
    super(
        uuid,
        createdAt,
        updatedAt,
        jobUuid,
        name,
        jobContextUuid,
        inputUuids,
        outputUuids,
        location,
        version,
        latestRunUuid,
        namespaceUuid,
        namespaceName);
    this.context = context;
    this.namespaceName = namespaceName;
    this.name = name;
  }
}
