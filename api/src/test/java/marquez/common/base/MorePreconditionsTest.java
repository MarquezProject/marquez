/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.common.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
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
