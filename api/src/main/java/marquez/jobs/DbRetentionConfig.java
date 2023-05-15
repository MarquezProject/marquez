/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import lombok.Getter;
import lombok.Setter;

/** Configuration for {@link DbRetentionJob}. */
public final class DbRetentionConfig {
  public static final int DEFAULT_FREQUENCY_MINS = 15;
  public static final int DEFAULT_RETENTION_DAYS = 7;

  @Getter @Setter private int frequencyMins = DEFAULT_FREQUENCY_MINS;
  @Getter @Setter private int retentionDays = DEFAULT_RETENTION_DAYS;
}
