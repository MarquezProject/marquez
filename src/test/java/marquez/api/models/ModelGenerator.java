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

package marquez.api.models;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.common.models.ModelGenerator.newConnectionUrl;
import static marquez.common.models.ModelGenerator.newConnectionUrlFor;
import static marquez.common.models.ModelGenerator.newContext;
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newFields;
import static marquez.common.models.ModelGenerator.newJobType;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newRunId;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newSourceType;
import static marquez.common.models.ModelGenerator.newTags;

import java.util.List;
import java.util.stream.Stream;
import marquez.Generator;
import marquez.common.models.DatasetName;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;

public final class ModelGenerator extends Generator {
  private ModelGenerator() {}

  public static final RunRequest EMPTY_RUN_REQUEST = new RunRequest(null, null, null);

  public static NamespaceRequest newNamespaceRequest() {
    return newNamespaceRequest(true);
  }

  public static NamespaceRequest newNamespaceRequest(final boolean hasDescription) {
    return new NamespaceRequest(
        newOwnerName().getValue(), hasDescription ? newDescription() : null);
  }

  public static List<NamespaceResponse> newNamespaceResponses(final int limit) {
    return Stream.generate(() -> newNamespaceResponse(true))
        .limit(limit)
        .collect(toImmutableList());
  }

  public static NamespaceResponse newNamespaceResponse() {
    return newNamespaceResponse(true);
  }

  public static NamespaceResponse newNamespaceResponse(final boolean hasDescription) {
    final String timeAsIso = newIsoTimestamp();
    return new NamespaceResponse(
        newNamespaceName().getValue(),
        timeAsIso,
        timeAsIso,
        newOwnerName().getValue(),
        hasDescription ? newDescription() : null);
  }

  public static SourceRequest newSourceRequest() {
    return newSourceRequest(newSourceType(), true);
  }

  public static SourceRequest newSourceRequest(
      final SourceType type, final boolean hasDescription) {
    return new SourceRequest(
        newSourceType().toString(),
        newConnectionUrlFor(type).toASCIIString(),
        hasDescription ? newDescription() : null);
  }

  public static SourceRequest newSourceRequestWith(final SourceType type) {
    return newSourceRequest(type, true);
  }

  public static List<SourceResponse> newSourceResponses(final int limit) {
    return Stream.generate(() -> newSourceResponse(true)).limit(limit).collect(toImmutableList());
  }

  public static SourceResponse newSourceResponse() {
    return newSourceResponse(true);
  }

  public static SourceResponse newSourceResponse(final boolean hasDescription) {
    final String timeAsIso = newIsoTimestamp();
    return new SourceResponse(
        newSourceType().toString(),
        newSourceName().getValue(),
        timeAsIso,
        timeAsIso,
        newConnectionUrl().toASCIIString(),
        hasDescription ? newDescription() : null);
  }

  public static DbTableRequest newDbTableRequestWith(
      final DatasetName physicalName, final SourceName sourceName) {
    return newDbTableRequestWith(physicalName, sourceName, true, true, true, false);
  }

  public static DbTableRequest newDbTableRequestWith(
      final DatasetName physicalName,
      final SourceName sourceName,
      final boolean hasFields,
      final boolean hasTags,
      final boolean hasDescription,
      final boolean hasRunId) {
    final String timeAsIso = newIsoTimestamp();
    return new DbTableRequest(
        physicalName.getValue(),
        sourceName.getValue(),
        hasFields ? newFields(4) : null,
        hasTags ? newTags(2) : null,
        hasDescription ? newDescription() : null,
        hasRunId ? newRunId().toString() : null);
  }

  public static StreamRequest newStreamRequestWith(
      final DatasetName physicalName, final SourceName sourceName) {
    return newStreamRequestWith(physicalName, sourceName, true, true, false, false);
  }

  public static StreamRequest newStreamRequestWith(
      final DatasetName physicalName,
      final SourceName sourceName,
      final boolean hasFields,
      final boolean hasTags,
      final boolean hasDescription,
      final boolean hasRunId) {
    final String timeAsIso = newIsoTimestamp();
    return new StreamRequest(
        physicalName.getValue(),
        sourceName.getValue(),
        newLocation().toString(),
        hasFields ? newFields(4) : null,
        hasTags ? newTags(2) : null,
        hasDescription ? newDescription() : null,
        hasRunId ? newRunId().toString() : null);
  }

  public static JobRequest newJobRequestWith(
      final List<DatasetName> inputs, final List<DatasetName> outputs) {
    return newJobRequestWith(inputs, outputs, true);
  }

  public static JobRequest newJobRequestWith(
      final List<DatasetName> inputs,
      final List<DatasetName> outputs,
      final boolean hasDescription) {
    return new JobRequest(
        newJobType().toString(),
        inputs.stream().map(DatasetName::getValue).collect(toImmutableList()),
        outputs.stream().map(DatasetName::getValue).collect(toImmutableList()),
        newLocation().toString(),
        newContext(),
        hasDescription ? newDescription() : null);
  }

  public static String newIsoTimestamp() {
    return ISO_INSTANT.format(newTimestamp());
  }
}
