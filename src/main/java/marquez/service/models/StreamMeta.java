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

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;
import static marquez.common.models.DatasetType.STREAM;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class StreamMeta extends DatasetMeta {
  @Getter private final URL schemaLocation;

  public StreamMeta(
      final DatasetName physicalName,
      final SourceName sourceName,
      @NonNull final URL schemaLocation,
      @Nullable final ImmutableList<Field> fields,
      @Nullable final ImmutableSet<TagName> tags,
      @Nullable final String description,
      @Nullable final RunId runId) {
    super(STREAM, physicalName, sourceName, fields, tags, description, runId);
    this.schemaLocation = schemaLocation;
  }

  @Override
  public UUID version(@NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                namespaceName.getValue(),
                getSourceName().getValue(),
                datasetName.getValue(),
                getPhysicalName().getValue(),
                schemaLocation.toString(),
                getFields().stream().map(DatasetMeta::joinField).collect(joining(VERSION_DELIM)))
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }
}
