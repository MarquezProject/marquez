/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.jobs;

import static marquez.db.DbRetention.DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
import static marquez.db.DbRetention.DEFAULT_RETENTION_DAYS;
import static marquez.jobs.DbRetentionConfig.DEFAULT_FREQUENCY_MINS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

/** The test suite for {@link DbRetentionConfig}. */
public class DbRetentionConfigTest {
  private static final Validator VALIDATOR =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testNewDbRetentionConfig_withDefaultsOnly() {
    final DbRetentionConfig configWithDefaults = new DbRetentionConfig();

    assertThat(configWithDefaults.getFrequencyMins()).isEqualTo(DEFAULT_FREQUENCY_MINS);
    assertThat(configWithDefaults.getNumberOfRowsPerBatch())
        .isEqualTo(DEFAULT_NUMBER_OF_ROWS_PER_BATCH);
    assertThat(configWithDefaults.getRetentionDays()).isEqualTo(DEFAULT_RETENTION_DAYS);
  }

  @Test
  public void testNewDbRetentionConfig_overrideFrequencyMins() {
    final int frequencyMinsOverride = 5;
    final DbRetentionConfig configWithFrequencyMinsOverride =
        DbRetentionConfig.builder().frequencyMins(frequencyMinsOverride).build();

    // No constraint violations.
    final Set<ConstraintViolation<DbRetentionConfig>> violations =
        VALIDATOR.validate(configWithFrequencyMinsOverride);
    assertThat(violations).isEmpty();

    assertThat(configWithFrequencyMinsOverride.getFrequencyMins()).isEqualTo(frequencyMinsOverride);
    assertThat(configWithFrequencyMinsOverride.getNumberOfRowsPerBatch())
        .isEqualTo(DEFAULT_NUMBER_OF_ROWS_PER_BATCH);
    assertThat(configWithFrequencyMinsOverride.getRetentionDays())
        .isEqualTo(DEFAULT_RETENTION_DAYS);
  }

  @Test
  public void testNewDbRetentionConfig_overrideNumberOfRowsPerBatch() {
    final int numberOfRowsPerBatchOverride = 25;
    final DbRetentionConfig configWithNumberOfRowsPerBatchOverride =
        DbRetentionConfig.builder().numberOfRowsPerBatch(numberOfRowsPerBatchOverride).build();

    // No constraint violations.
    final Set<ConstraintViolation<DbRetentionConfig>> violations =
        VALIDATOR.validate(configWithNumberOfRowsPerBatchOverride);
    assertThat(violations).isEmpty();

    assertThat(configWithNumberOfRowsPerBatchOverride.getFrequencyMins())
        .isEqualTo(DEFAULT_FREQUENCY_MINS);
    assertThat(configWithNumberOfRowsPerBatchOverride.getNumberOfRowsPerBatch())
        .isEqualTo(numberOfRowsPerBatchOverride);
    assertThat(configWithNumberOfRowsPerBatchOverride.getRetentionDays())
        .isEqualTo(DEFAULT_RETENTION_DAYS);
  }

  @Test
  public void testNewDbRetentionConfig_overrideRetentionDays() {
    final int retentionDaysOverride = 14;
    final DbRetentionConfig configWithNumberOfRowsPerBatchOverride =
        DbRetentionConfig.builder().retentionDays(retentionDaysOverride).build();

    // No constraint violations.
    final Set<ConstraintViolation<DbRetentionConfig>> violations =
        VALIDATOR.validate(configWithNumberOfRowsPerBatchOverride);
    assertThat(violations).isEmpty();

    assertThat(configWithNumberOfRowsPerBatchOverride.getFrequencyMins())
        .isEqualTo(DEFAULT_FREQUENCY_MINS);
    assertThat(configWithNumberOfRowsPerBatchOverride.getNumberOfRowsPerBatch())
        .isEqualTo(DEFAULT_NUMBER_OF_ROWS_PER_BATCH);
    assertThat(configWithNumberOfRowsPerBatchOverride.getRetentionDays())
        .isEqualTo(retentionDaysOverride);
  }

  @Test
  public void testNewDbRetentionConfig_negativeFrequencyMins() {
    final int negativeFrequencyMins = -5;

    final DbRetentionConfig configWithNegativeFrequencyMins =
        DbRetentionConfig.builder().frequencyMins(negativeFrequencyMins).build();

    final Set<ConstraintViolation<DbRetentionConfig>> violations =
        VALIDATOR.validate(configWithNegativeFrequencyMins);
    assertThat(violations).hasSize(1);
  }

  @Test
  public void testNewDbRetentionConfig_negativeNumberOfRowsPerBatch() {
    final int negativeNumberOfRowsPerBatch = -25;

    final DbRetentionConfig configWithNegativeNumberOfRowsPerBatch =
        DbRetentionConfig.builder().numberOfRowsPerBatch(negativeNumberOfRowsPerBatch).build();

    final Set<ConstraintViolation<DbRetentionConfig>> violations =
        VALIDATOR.validate(configWithNegativeNumberOfRowsPerBatch);
    assertThat(violations).hasSize(1);
  }

  @Test
  public void testNewDbRetentionConfig_negativeRetentionDays() {
    final int negativeRetentionDays = -14;

    final DbRetentionConfig configWithNegativeRetentionDays =
        DbRetentionConfig.builder().retentionDays(negativeRetentionDays).build();

    final Set<ConstraintViolation<DbRetentionConfig>> violations =
        VALIDATOR.validate(configWithNegativeRetentionDays);
    assertThat(violations).hasSize(1);
  }
}
