/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class MorePreconditionsTest {
  private static final String NULL_ERROR_MESSAGE = null;
  private static final String NON_NULL_ERROR_MESSAGE = "test error message";
  private static final String NON_NULL_ERROR_MESSAGE_WITH_ARGS = "test error message with %s";
  private static final String ARG = "test arg";
  private static final String BLANK_STRING = " ";
  private static final String EMPTY_STRING = "";
  private static final String NON_BLANK_STRING = "test string";
  private static final String NULL_STRING = null;

  @Test
  public void testNotBlank() {
    assertThat(MorePreconditions.checkNotBlank(NON_BLANK_STRING)).isEqualTo(NON_BLANK_STRING);
  }

  @Test
  public void testCheckNotBlank_throwsOnNullString_noErrorMessage() {
    assertThatNullPointerException().isThrownBy(() -> MorePreconditions.checkNotBlank(NULL_STRING));
  }

  @Test
  public void testCheckNotBlank_throwsOnBlankString_noErrorMessage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> MorePreconditions.checkNotBlank(BLANK_STRING));
  }

  @Test
  public void testCheckNotBlank_throwsOnEmptyString_noErrorMessage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> MorePreconditions.checkNotBlank(EMPTY_STRING));
  }

  @Test
  public void testCheckNotBlank_throwsOnNullString_withErrorMessage() {
    assertThatNullPointerException()
        .isThrownBy(() -> MorePreconditions.checkNotBlank(NULL_STRING, NON_NULL_ERROR_MESSAGE));
  }

  @Test
  public void testCheckNotBlank_throwsOnBlankString_withErrorMessage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> MorePreconditions.checkNotBlank(BLANK_STRING, NON_NULL_ERROR_MESSAGE));
  }

  @Test
  public void testCheckNotBlank_throwsOnEmptyString_withErrorMessage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> MorePreconditions.checkNotBlank(EMPTY_STRING, NON_NULL_ERROR_MESSAGE));
  }

  @Test
  public void testCheckNotBlank_throwsOnNullString_nullErrorMessage() {
    assertThatNullPointerException()
        .isThrownBy(() -> MorePreconditions.checkNotBlank(NULL_STRING, NULL_ERROR_MESSAGE));
  }

  @Test
  public void testCheckNotBlank_throwsOnEmptyString_nullErrorMessage() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> MorePreconditions.checkNotBlank(EMPTY_STRING, NULL_ERROR_MESSAGE));
  }

  @Test
  public void testCheckNotBlank_throwsOnNullString_errorMessageWithArgs() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                MorePreconditions.checkNotBlank(
                    NULL_STRING, NON_NULL_ERROR_MESSAGE_WITH_ARGS, ARG));
  }

  @Test
  public void testCheckNotBlank_throwsOnBlankString_errorMessageWithArgs() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                MorePreconditions.checkNotBlank(
                    BLANK_STRING, NON_NULL_ERROR_MESSAGE_WITH_ARGS, ARG));
  }

  @Test
  public void testCheckNotBlank_throwsOnEmptyString_errorMessageWithArgs() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                MorePreconditions.checkNotBlank(
                    EMPTY_STRING, NON_NULL_ERROR_MESSAGE_WITH_ARGS, ARG));
  }
}
