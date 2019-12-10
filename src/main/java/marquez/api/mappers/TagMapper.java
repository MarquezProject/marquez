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

import java.time.Instant;
import lombok.NonNull;
import marquez.api.models.Tag;
import marquez.common.models.TagName;

public class TagMapper {
  public static Tag map(@NonNull marquez.service.models.Tag tag, @NonNull Instant taggedAt) {
    return new Tag(tag.getName(), taggedAt.toString());
  }

  public static marquez.service.models.Tag map(@NonNull Tag tag) {
    return marquez.service.models.Tag.builder().name(TagName.fromString(tag.getName())).build();
  }
}
