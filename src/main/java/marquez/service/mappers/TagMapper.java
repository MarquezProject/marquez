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

package marquez.service.mappers;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.NonNull;
import marquez.common.models.TagName;
import marquez.db.models.DatasetRowExtended;
import marquez.db.models.TagRow;
import marquez.service.models.Tag;

public final class TagMapper {
  private TagMapper() {}

  public static Tag map(@NonNull TagRow row) {
    return Tag.builder()
        .name(TagName.fromString(row.getName()))
        .description(row.getDescription())
        .build();
  }

  public static Tag map(@NonNull DatasetRowExtended inputRow) {
    if (inputRow.getTaggedAt() == null) {
      return null;
    }
    return Tag.builder()
        .name(TagName.fromString(inputRow.getTagName()))
        .taggedAt(inputRow.getTaggedAt())
        .build();
  }

  public static List<Tag> map(@NonNull List<TagRow> rows) {
    return unmodifiableList(rows.stream().map(row -> map(row)).collect(toList()));
  }
}
