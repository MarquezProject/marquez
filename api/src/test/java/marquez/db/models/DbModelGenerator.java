/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.Generator;
import marquez.common.models.JobId;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import org.jetbrains.annotations.NotNull;

/** Generates new instances for {@code marquez.db.models} with random values used for testing. */
public final class DbModelGenerator extends Generator {
  private DbModelGenerator() {}

  /** Returns new {@link NamespaceRow} objects with a specified {@code limit}. */
  public static List<NamespaceRow> newNamespaceRows(int limit) {
    return Stream.generate(() -> newNamespaceRow()).limit(limit).collect(toImmutableList());
  }

  /** Returns a new {@link NamespaceRow} object. */
  public static NamespaceRow newNamespaceRow() {
    final Instant now = newTimestamp();
    return new NamespaceRow(
        newRowUuid(),
        now,
        now,
        newNamespaceName().getValue(),
        newDescription(),
        newOwnerName().getValue());
  }

  /** Returns a new {@code row} uuid. */
  public static UUID newRowUuid() {
    return UUID.randomUUID();
  }

  @NotNull
  public static JobId jobIdFor(String namespaceName, String jobName) {
    return new JobId(new NamespaceName(namespaceName), new JobName(jobName));
  }
}
