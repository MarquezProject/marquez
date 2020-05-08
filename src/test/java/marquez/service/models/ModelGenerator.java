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

package marquez.service.models;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newTagName;

import com.google.common.collect.ImmutableSet;
import java.util.stream.Stream;
import marquez.Generator;

public final class ModelGenerator extends Generator {
  private ModelGenerator() {}

  public static ImmutableSet<Tag> newTags(final int limit) {
    return Stream.generate(() -> newTag()).limit(limit).collect(toImmutableSet());
  }

  public static Tag newTag() {
    return new Tag(newTagName(), newDescription());
  }
}
