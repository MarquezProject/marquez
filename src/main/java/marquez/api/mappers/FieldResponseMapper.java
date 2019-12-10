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

package marquez.api.mappers;

import java.util.stream.Collectors;
import marquez.common.models.Field;

public class FieldResponseMapper {
  public static marquez.api.models.Field map(Field field) {
    return new marquez.api.models.Field(
        field.getName().toString(),
        field.getType().toString(),
        field.getCreatedAt().toString(),
        field.getTags().stream()
            .map(tag -> new marquez.api.models.Tag(tag.getName(), tag.getTaggedAt()))
            .collect(Collectors.toList()),
        field.getDescription().get());
  }
}
