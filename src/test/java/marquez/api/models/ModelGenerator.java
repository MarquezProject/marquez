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
import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newJobType;
import static marquez.common.models.ModelGenerator.newLocation;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.common.models.ModelGenerator.newOwnerName;
import static marquez.common.models.ModelGenerator.newRunId;
import static marquez.common.models.ModelGenerator.newSourceName;
import static marquez.common.models.ModelGenerator.newSourceType;

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

  public static SourceRequest newSourceRequestWith(final SourceType type) {
    return newSourceRequest(type, true);
  }

  public static SourceRequest newSourceRequest(
      final SourceType type, final boolean hasDescription) {
    return new SourceRequest(
        newSourceType().toString(),
        newConnectionUrlFor(type).toASCIIString(),
        hasDescription ? newDescription() : null);
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
    return newDbTableRequestWith(physicalName, sourceName, true, false);
  }

  public static DbTableRequest newDbTableRequestWith(
      final DatasetName physicalName,
      final SourceName sourceName,
      final boolean hasDescription,
      final boolean hasRunId) {
    final String timeAsIso = newIsoTimestamp();
    return new DbTableRequest(
        physicalName.getValue(),
        sourceName.getValue(),
        hasDescription ? newDescription() : null,
        hasRunId ? newRunId().toString() : null);
  }

  public static StreamRequest newStreamRequestWith(
      final DatasetName physicalName, final SourceName sourceName) {
    return newStreamRequestWith(physicalName, sourceName, true, false);
  }

  public static StreamRequest newStreamRequestWith(
      final DatasetName physicalName,
      final SourceName sourceName,
      final boolean hasDescription,
      final boolean hasRunId) {
    final String timeAsIso = newIsoTimestamp();
    return new StreamRequest(
        physicalName.getValue(),
        sourceName.getValue(),
        newLocation().toString(),
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
        hasDescription ? newDescription() : null);
  }

  public static String newIsoTimestamp() {
    return ISO_INSTANT.format(newTimestamp());
  }

  ////////////////

  // public static DatasetRequest newDatasetRequest() {
  //   return newDatasetRequest(true);
  // }
  //
  // public static DatasetRequest newDatasetRequest(final boolean hasDescription) {
  //   return new DatasetRequest(
  //       newDatasetName().getValue(),
  //       newDatasourceUrn().getValue(),
  //       hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  // }
  //
  // public static List<DatasetResponse> newDatasetResponses(final int limit) {
  //   return Stream.generate(() -> newDatasetResponse()).limit(limit).collect(toImmutableList());
  // }
  //
  // public static DatasetResponse newDatasetResponse() {
  //   return newDatasetResponse(true);
  // }
  //
  // public static DatasetResponse newDatasetResponse(final boolean hasDescription) {
  //   return new DatasetResponse(
  //       newDatasetName().getValue(),
  //       newDatasetUrn().getValue(),
  //       newIsoTimestamp(),
  //       newDatasourceUrn().getValue(),
  //       hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  // }
  //
  //
  //

  // }
  //
  // public static List<JobResponse> newJobResponses(final int limit) {
  //   return Stream.generate(() -> newJobResponse(true)).limit(limit).collect(toImmutableList());
  // }
  //
  // public static JobResponse newJobResponse() {
  //   return newJobResponse(true);
  // }
  //
  // public static JobResponse newJobResponse(final boolean hasDescription) {
  //   final String createdAt = newIsoTimestamp();
  //   final String updatedAt = createdAt;
  //   return new JobResponse(
  //       newJobType().toString(),
  //       newJobName().getValue(),
  //       createdAt,
  //       updatedAt,
  //       newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toImmutableList()),
  //       newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toImmutableList()),
  //       newLocation().toString(),
  //       hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  // }
  //

  //
  // public static DatasourceRequest newDatasourceRequest() {
  //   return new DatasourceRequest(newDatasourceName().getValue(),
  // newConnectionUrl().getRawValue());
  // }
  //
  // public static List<DatasourceResponse> newDatasourceResponses(final int limit) {
  //   return Stream.generate(() ->
  // newDatasourceResponse()).limit(limit).collect(toImmutableList());
  // }
  //
  // public static DatasourceResponse newDatasourceResponse() {
  //   return new DatasourceResponse(
  //       newDatasourceName().getValue(),
  //       newIsoTimestamp(),
  //       newDatasourceUrn().getValue(),
  //       newConnectionUrl().getRawValue());
  // }
  //
  //
}
