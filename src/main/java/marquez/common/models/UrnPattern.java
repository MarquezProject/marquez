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

package marquez.common.models;

import static marquez.common.Preconditions.checkNotBlank;

import java.util.regex.Pattern;
import lombok.NonNull;

public final class UrnPattern {
  private static final int URN_MIN_SIZE = 1;
  private static final int URN_MAX_SIZE = 64;

  public static final String URN_DELIM = ":";
  public static final String URN_PREFIX = "urn";

  private final Pattern pattern;

  private UrnPattern(@NonNull final String value) {
    this.pattern = Pattern.compile(checkNotBlank(value));
  }

  public static UrnPattern from(@NonNull UrnType type) {
    final String value =
        String.format(
            "^%s%s%s(%s[a-zA-Z0-9._]{%d,%d}){%d}$",
            URN_PREFIX,
            URN_DELIM,
            type.toString(),
            URN_DELIM,
            URN_MIN_SIZE,
            URN_MAX_SIZE,
            type.numberOfParts());
    return new UrnPattern(value);
  }

  public void throwIfNoMatch(@NonNull String value) {
    if (!pattern.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A urn must contain only letters (a-z, A-Z), numbers (0-9), or underscores (_) and "
              + "be sperated by colons (:) with each part having a maximum length of 64 characters.");
    }
  }
}
