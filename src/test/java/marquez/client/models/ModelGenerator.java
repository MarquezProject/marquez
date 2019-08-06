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

package marquez.client.models;

import static java.util.stream.Collectors.toList;
import static marquez.client.models.RunState.NEW;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

public final class ModelGenerator {
  private ModelGenerator() {}

  private static final Random RANDOM = new Random();

  public static String newOwnerName() {
    return "test_owner" + newId();
  }

  public static String newNamespaceName() {
    return "test_namespace" + newId();
  }

  public static String newJobName() {
    return "test_job" + newId();
  }

  public static String newLocation() {
    return "https://github.com/repo/test/commit/" + newId();
  }

  public static String newDatasourceName() {
    return "test_datasource" + newId();
  }

  public static String newDatasourceUrn() {
    return String.format("urn:datasource:%s:%s" + newId(), newDatasourceType(), newDbName());
  }

  public static String newConnectionUrl() {
    return String.format("jdbc:%s://localhost:5431/%s" + newId(), newDatasourceType(), newDbName());
  }

  public static String newDatasetName() {
    return "test_dataset" + newId();
  }

  public static List<String> newDatasetUrns(int limit) {
    return Stream.generate(() -> newDatasetUrn()).limit(limit).collect(toList());
  }

  public static String newDatasetUrn() {
    return String.format("urn:dataset:%s:public.room_bookings" + newId(), newDbName());
  }

  public static String newDescription() {
    return "test_description" + newId();
  }

  public static String newRunArgs() {
    return String.format("--email=wedata%s@wework.com", newId());
  }

  public static String newRunId() {
    return UUID.randomUUID().toString();
  }

  private static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  private static String newDatasourceType() {
    List<String> datasourceTypes = Arrays.asList("redshift", "mysql", "postgresql");
    return datasourceTypes.get(RANDOM.nextInt(datasourceTypes.size()));
  }

  private static String newDbName() {
    return "test_db" + newId();
  }

  public static Namespace newNamespace() {
    return new Namespace(newNamespaceName(), Instant.now(), newOwnerName(), newDescription());
  }

  public static Job newJob() {
    final Instant now = Instant.now();
    return new Job(
        newJobName(),
        now,
        now,
        newDatasetUrns(3),
        newDatasetUrns(4),
        newLocation(),
        newDescription());
  }

  public static JobRun newJobRun() {
    final Instant now = Instant.now();
    return new JobRun(newRunId(), now, now, newRunArgs(), NEW);
  }

  public static Datasource newDatasource() {
    final Instant now = Instant.now();
    return new Datasource(newDatasourceName(), now, newDatasetUrn(), newConnectionUrl());
  }

  public static Dataset newDataset() {
    final Instant now = Instant.now();
    return new Dataset(
        newDatasetName(), now, newDatasetUrn(), newDatasourceUrn(), newDescription());
  }

  public static List<Namespace> newNamespaces(int limit) {
    return Stream.generate(() -> newNamespace()).limit(limit).collect(toList());
  }

  public static List<Job> newJobs(int limit) {
    return Stream.generate(() -> newJob()).limit(limit).collect(toList());
  }

  public static List<JobRun> newJobRuns(int limit) {
    return Stream.generate(() -> newJobRun()).limit(limit).collect(toList());
  }

  public static List<Datasource> newDatasources(int limit) {
    return Stream.generate(() -> newDatasource()).limit(limit).collect(toList());
  }

  public static List<Dataset> newDatasets(int limit) {
    return Stream.generate(() -> newDataset()).limit(limit).collect(toList());
  }
}
