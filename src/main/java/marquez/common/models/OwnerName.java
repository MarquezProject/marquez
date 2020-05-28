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
@JsonDeserialize(converter = OwnerName.FromValue.class)
@JsonSerialize(converter = OwnerName.ToValue.class)
public final class OwnerName {
  @Getter private final String value;

  public OwnerName(@NonNull final String value) {
    this.value = checkNotBlank(value, "value must not be blank");
  }

  public static OwnerName of(final String value) {
    return new OwnerName(value);
  }

  public static class FromValue extends StdConverter<String, OwnerName> {
    @Override
    public OwnerName convert(@NonNull String value) {
      return OwnerName.of(value);
    }
  }

  public static class ToValue extends StdConverter<OwnerName, String> {
    @Override
    public String convert(@NonNull OwnerName name) {
      return name.getValue();
    }
  }

  public static final OwnerName ANONYMOUS = OwnerName.of("anonymous");
}
