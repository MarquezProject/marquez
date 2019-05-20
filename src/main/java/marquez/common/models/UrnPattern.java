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

import static marquez.common.Preconditions.checkArgument;
import static marquez.common.Preconditions.checkNotBlank;

import java.util.regex.Pattern;
import lombok.NonNull;

final class UrnPattern {
  private static final int MIN_SIZE = 1;
  private static final int MAX_SIZE = 64;

  static final String DELIM = ":";
  static final String PREFIX = "urn";

  private final Pattern pattern;

  private UrnPattern(@NonNull final String value) {
    this.pattern = Pattern.compile(checkNotBlank(value));
  }

  static UrnPattern from(@NonNull String namespace, @NonNull Integer numOfParts) {
    checkNotBlank(namespace);
    checkArgument(numOfParts > 0, "numOfParts must be > 0");
    final String value =
        String.format(
            "^%s%s%s(%s[a-zA-Z0-9._-]{%d,%d}){%d}$",
            PREFIX, DELIM, namespace, DELIM, MIN_SIZE, MAX_SIZE, numOfParts);
    return new UrnPattern(value);
  }

  void throwIfNoMatch(@NonNull String value) {
    if (!pattern.matcher(checkNotBlank(value)).matches()) {
      throw new IllegalArgumentException(
          String.format(
              "urn (%s) must contain only letters (a-z, A-Z), numbers (0-9), periods (.), "
                  + "underscores (_) or dashes (-) and be sperated by colons (:) with each part "
                  + "having a maximum length of 64 characters",
              value));
    }
  }
}
