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

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = SourceName.FromValue.class)
@JsonSerialize(converter = SourceName.ToValue.class)
public final class SourceName {
  @Getter private final String value;

  public SourceName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static SourceName of(final String value) {
    return new SourceName(value);
  }

  public static class FromValue extends StdConverter<String, SourceName> {
    @Override
    public SourceName convert(@NonNull String value) {
      return SourceName.of(value);
    }
  }

  public static class ToValue extends StdConverter<SourceName, String> {
    @Override
    public String convert(@NonNull SourceName name) {
      return name.getValue();
    }
  }
}
