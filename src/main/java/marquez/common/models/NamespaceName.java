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

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class NamespaceName {
  private static final int NAMESPACE_MIN_SIZE = 1;
  private static final int NAMESPACE_MAX_SIZE = 1024;
  private static final Pattern NAMESPACE_PATTERN =
      Pattern.compile(
          String.format("^[a-zA-Z0-9_]{%d,%d}$", NAMESPACE_MIN_SIZE, NAMESPACE_MAX_SIZE));

  @Getter private final String value;

  private NamespaceName(@NonNull final String value) {
    if (!NAMESPACE_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "A namespaces must contain only letters (a-z, A-Z), numbers (0-9), or "
              + "underscores (_) with a maximum length of 1024 characters.");
    }

    this.value = value;
  }

  @JsonCreator
  public static NamespaceName fromString(String value) {
    return new NamespaceName(value);
  }
}
