/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import static marquez.db.DbRetention.DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
import static marquez.db.DbRetention.DEFAULT_RETENTION_DAYS;

import javax.validation.constraints.Positive;
import lombok.Getter;
import lombok.Value;

/** Configuration for {@link DbRetentionJob}. */
@Value
public class DbRetentionConfig {
  public static final int DEFAULT_FREQUENCY_MINS = 15;

  @Getter @Positive int frequencyMins = DEFAULT_FREQUENCY_MINS;
  @Getter @Positive int numberOfRowsPerBatch = DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
  @Getter @Positive int retentionDays = DEFAULT_RETENTION_DAYS;
}
