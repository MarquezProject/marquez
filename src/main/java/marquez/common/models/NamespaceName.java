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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class NamespaceName {
  private static final int MIN_SIZE = 1;
  private static final int MAX_SIZE = 1024;
  private static final Pattern PATTERN =
      Pattern.compile(String.format("^[a-zA-Z0-9_-]{%d,%d}$", MIN_SIZE, MAX_SIZE));

  @Getter private final String value;

  private NamespaceName(final String value) {
    checkArgument(
        PATTERN.matcher(value).matches(),
        "namespaces (%s) must contain only letters (a-z, A-Z), numbers (0-9), "
            + "underscores (_) or dashes (-) with a maximum length of 1024 characters.",
        value);
    this.value = value;
  }

  public static NamespaceName of(final String value) {
    return new NamespaceName(value);
  }

  public static final NamespaceName DEFAULT = NamespaceName.of("default");
}
