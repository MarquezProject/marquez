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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class MorePreconditionsTest {
  private static final String NULL_ERROR_MESSAGE = null;
  private static final String NON_NULL_ERROR_MESSAGE = "test error message";
  private static final String NON_NULL_ERROR_MESSAGE_WITH_ARGS = "test error message with %s";
  private static final String BLANK_ERROR_MESSAGE = "object is blank";
  private static final String BLANK_STRING = " ";
  private static final String NON_BLANK_STRING = "test string";
  private static final String NULL_STRING = null;
  private static final String NON_NULL_STRING = NON_BLANK_STRING;
  private static final List<String> NON_EMPTY_LIST =
      new ArrayList(1) {
        {
          add("one");
        }
      };
  private static final List<String> EMPTY_LIST = Collections.emptyList();
  private static final List<String> NULL_LIST = null;

  @Test
  public void testCheckNotBlank_noErrorMessage() {
    MorePreconditions.checkNotBlank(NON_BLANK_STRING);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotBlank_throwsException_nullString_noErrorMessage() {
    MorePreconditions.checkNotBlank(NULL_STRING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotBlank_throwsException_noErrorMessage() {
    MorePreconditions.checkNotBlank(BLANK_STRING);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotBlank_throwsException_nullErrorMessage() {
    MorePreconditions.checkNotBlank(BLANK_STRING, NULL_ERROR_MESSAGE);
  }

  @Test
  public void testCheckNotBlank_withErrorMessage() {
    MorePreconditions.checkNotBlank(NON_BLANK_STRING, NON_NULL_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotBlank_throwsException_nullString_nullErrorMessage() {
    MorePreconditions.checkNotBlank(NULL_STRING, NULL_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotBlank_throwsException_nullString_withErrorMessage() {
    MorePreconditions.checkNotBlank(NULL_STRING, NON_NULL_STRING);
  }

  @Test
  public void testCheckNotBlank_nullErrorMessage() {
    MorePreconditions.checkNotBlank(NON_BLANK_STRING, NULL_ERROR_MESSAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkNotBlank_throwsException_errorMessageNoArgs() {
    MorePreconditions.checkNotBlank(BLANK_STRING, NON_NULL_ERROR_MESSAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void checkNotBlank_throwsException_errorMessageWithArgs() {
    MorePreconditions.checkNotBlank(BLANK_STRING, NON_NULL_ERROR_MESSAGE_WITH_ARGS, "foo");
  }

  @Test
  public void testCheckNotEmpty_noErrorMessage() {
    MorePreconditions.checkNotEmpty(NON_EMPTY_LIST);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotEmpty_emptyList_noErrorMessage() {
    MorePreconditions.checkNotEmpty(EMPTY_LIST);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotEmpty_nullCollection_noErrorMessage() {
    MorePreconditions.checkNotEmpty(NULL_LIST);
  }

  @Test
  public void testCheckNotEmpty_withErrorMessage() {
    MorePreconditions.checkNotEmpty(NON_EMPTY_LIST, NON_NULL_ERROR_MESSAGE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCheckNotEmpty_emptyList_withErrorMessage() {
    MorePreconditions.checkNotEmpty(EMPTY_LIST, BLANK_ERROR_MESSAGE);
  }

  @Test(expected = NullPointerException.class)
  public void testCheckNotEmpty_nullCollection_withErrorMessage() {
    MorePreconditions.checkNotEmpty(NULL_LIST, NULL_ERROR_MESSAGE);
  }
}
