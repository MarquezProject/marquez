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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = NamespaceName.FromValue.class)
@JsonSerialize(converter = NamespaceName.ToValue.class)
public final class NamespaceName {
  private static final int MIN_SIZE = 1;
  private static final int MAX_SIZE = 1024;
  private static final Pattern PATTERN =
      Pattern.compile(String.format("^[a-zA-Z:/0-9_\\-\\.]{%d,%d}$", MIN_SIZE, MAX_SIZE));

  @Getter private final String value;

  public NamespaceName(@NonNull final String value) {
    checkArgument(
        PATTERN.matcher(value).matches(),
        "namespace '%s' must contain only letters (a-z, A-Z), numbers (0-9), "
            + "underscores (_), dashes (-), or dots (.) with a maximum length of %s characters.",
        value,
        MAX_SIZE);
    this.value = value;
  }

  public static NamespaceName of(final String value) {
    return new NamespaceName(value);
  }

  public static class FromValue extends StdConverter<String, NamespaceName> {
    @Override
    public NamespaceName convert(@NonNull String value) {
      return NamespaceName.of(value);
    }
  }

  public static class ToValue extends StdConverter<NamespaceName, String> {
    @Override
    public String convert(@NonNull NamespaceName name) {
      return name.getValue();
    }
  }

  public static final NamespaceName DEFAULT = NamespaceName.of("default");
}
