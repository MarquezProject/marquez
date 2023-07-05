/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import static marquez.db.DbRetention.DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
import static marquez.db.DbRetention.DEFAULT_RETENTION_DAYS;

import lombok.Getter;
import lombok.Setter;

/** Configuration for {@link DbRetentionJob}. */
public final class DbRetentionConfig {
  public static final int DEFAULT_FREQUENCY_MINS = 15;

  @Getter @Setter private int frequencyMins = DEFAULT_FREQUENCY_MINS;
  @Getter @Setter private int numberOfRowsPerBatch = DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
  @Getter @Setter private int retentionDays = DEFAULT_RETENTION_DAYS;
}
