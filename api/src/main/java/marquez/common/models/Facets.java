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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/** Defines the {@code facets} type. */
@EqualsAndHashCode
@ToString
@JsonSerialize(converter = Facets.ToValue.class)
public final class Facets {
  @Getter private final JsonNode value;

  /**
   * Returns a new {@code facets} instance containing the specified {@code value}.
   *
   * @param value The {@code facets} value.
   */
  public Facets(@NonNull final JsonNode value) {
    this.value = value;
  }

  /**
   * A factory method for instantiating a new {@code facets} object containing the specified {@code
   * value}.
   *
   * @param value The value of the {@code facets}.
   * @return A {@code facets} object.
   */
  public static Facets of(final JsonNode value) {
    return new Facets(value);
  }

  /**
   * An utility class that converts from {@code facets} object to {@link
   * com.fasterxml.jackson.databind.JsonNode} during serialization.
   */
  public static class ToValue extends StdConverter<Facets, JsonNode> {
    @Override
    public JsonNode convert(@NonNull Facets facets) {
      return facets.getValue();
    }
  }
}
