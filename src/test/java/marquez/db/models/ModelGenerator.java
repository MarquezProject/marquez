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

package marquez.db.models;

import static java.util.stream.Collectors.toList;
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newSourceType;
import static marquez.common.models.ModelGenerator.newTagName;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.Generator;
import marquez.common.Utils;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.common.models.TagName;

public final class ModelGenerator extends Generator {
  private ModelGenerator() {}

  public static List<TagRow> newTagRows(int limit) {
    return Stream.generate(() -> newTagRow()).limit(limit).collect(toList());
  }

  public static TagRow newTagRow() {
    return newTagRowWith(newTagName(), true, false);
  }

  public static TagRow newTagRowWith(TagName name, boolean hasDescription, boolean wasUpdated) {
    final TagRow.TagRowBuilder builder =
        TagRow.builder()
            .uuid(UUID.randomUUID())
            .createdAt(newTimestamp())
            .updatedAt(newTimestamp())
            .name(name.getValue());

    if (hasDescription) {
      builder.description(newDescription());
    }

    if (wasUpdated) {
      builder.updatedAt(newTimestamp());
    }

    return builder.build();
  }

  public static List<SourceRow> newSourceRows(final int limit) {
    return Stream.generate(() -> newSourceRow()).limit(limit).collect(toList());
  }

  public static SourceRow newSourceRow() {
    return newSourceRowWith(newSourceName());
  }

  public static SourceRow newSourceRowWith(final SourceName sourceName) {
    final Instant now = newTimestamp();
    final SourceType type = newSourceType();
    return new SourceRow(
        newRowUuid(),
        type.toString(),
        now,
        now,
        sourceName.getValue(),
        newConnectionUrlFor(type).toASCIIString(),
        newDescription());
  }

  public static List<JobContextRow> newJobContextRows(final int limit) {
    return Stream.generate(() -> newJobContextRow()).limit(limit).collect(toList());
  }

  public static JobContextRow newJobContextRow() {
    return newJobContextRowWith(newContext());
  }

  public static JobContextRow newJobContextRowWith(final Map<String, String> context) {
    return new JobContextRow(
        newRowUuid(), newTimestamp(), Utils.toJson(context), Utils.checksumFor(context));
  }

  public static UUID newRowUuid() {
    return UUID.randomUUID();
  }
}
