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

import static java.util.stream.Collectors.toList;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasetUrns;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.Description.NO_DESCRIPTION;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.ModelGenerator;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;

public final class ServiceModelGenerator extends ModelGenerator {
  private ServiceModelGenerator() {}

  public static List<Namespace> newNamespaces(final int limit) {
    return Stream.generate(() -> newNamespace()).limit(limit).collect(toList());
  }

  public static Namespace newNamespace() {
    return newNamespace(true);
  }

  public static Namespace newNamespace(final boolean hasDescription) {
    return new Namespace(
        null,
        newTimestamp(),
        newNamespaceName().getValue(),
        newOwnerName().getValue(),
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
  }

  public static List<Datasource> newDatasources(final int limit) {
    return Stream.generate(() -> newDatasource()).limit(limit).collect(toList());
  }

  public static Datasource newDatasource() {
    return newDatasourceWith(newDatasourceName(), newDatasourceUrn(), newConnectionUrl());
  }

  public static Datasource newDatasourceWith(
      final DatasourceName datasourceName,
      final DatasourceUrn datasourceUrn,
      final ConnectionUrl connectionUrl) {
    return Datasource.builder()
        .name(datasourceName)
        .createdAt(newTimestamp())
        .urn(datasourceUrn)
        .connectionUrl(connectionUrl)
        .build();
  }

  public static DatasetMeta newDatasetMeta() {
    return newDatasetMeta(true);
  }

  public static DatasetMeta newDatasetMeta(final boolean hasDescription) {
    return newDatasetMetaWith(
        newDatasetName(), newDatasourceUrn(), hasDescription ? newDescription() : NO_DESCRIPTION);
  }

  public static DatasetMeta newDatasetMetaWith(
      final DatasetName datasetName,
      final DatasourceUrn datasourceUrn,
      final Description description) {
    return DatasetMeta.builder()
        .name(datasetName)
        .datasourceUrn(datasourceUrn)
        .description(description)
        .build();
  }

  public static List<Dataset> newDatasets(final int limit) {
    return Stream.generate(() -> newDataset()).limit(limit).collect(toList());
  }

  public static Dataset newDataset() {
    return newDataset(true);
  }

  public static Dataset newDataset(final boolean hasDescription) {
    return newDatasetWith(
        newDatasetName(), newDatasetUrn(), hasDescription ? newDescription() : NO_DESCRIPTION);
  }

  public static Dataset newDatasetWith(
      final DatasetName datasetName, final DatasetUrn datasetUrn, final Description description) {
    return Dataset.builder()
        .name(datasetName)
        .createdAt(newTimestamp())
        .urn(datasetUrn)
        .datasourceUrn(newDatasourceUrn())
        .description(description)
        .build();
  }

  public static List<Job> newJobs(final int limit) {
    return Stream.generate(() -> newJob()).limit(limit).collect(toList());
  }

  public static Job newJob() {
    return newJob(true);
  }

  public static Job newJob(final boolean hasDescription) {
    final Instant createdAt = newTimestamp();
    final Instant updatedAt = createdAt;
    return new Job(
        null,
        newJobName().getValue(),
        newLocation().toString(),
        null,
        hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue(),
        newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toList()),
        newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toList()),
        createdAt,
        updatedAt);
  }

  public static List<JobRun> newJobRuns(final int limit) {
    return Stream.generate(() -> newJobRun()).limit(limit).collect(toList());
  }

  public static JobRun newJobRun() {
    return new JobRun(UUID.randomUUID(), 0, null, null, null, null, null, newTimestamp());
  }
}
