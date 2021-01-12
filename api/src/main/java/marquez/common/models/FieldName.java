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
@JsonDeserialize(converter = FieldName.FromValue.class)
@JsonSerialize(converter = FieldName.ToValue.class)
public final class FieldName {
  @Getter private final String value;

  public FieldName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static FieldName of(final String value) {
    return new FieldName(value);
  }

  public static class FromValue extends StdConverter<String, FieldName> {
    @Override
    public FieldName convert(@NonNull String value) {
      return FieldName.of(value);
    }
  }

  public static class ToValue extends StdConverter<FieldName, String> {
    @Override
    public String convert(@NonNull FieldName name) {
      return name.getValue();
    }
  }
}
