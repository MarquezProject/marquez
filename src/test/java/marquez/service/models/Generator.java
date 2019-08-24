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

import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasourceRow;
import marquez.db.models.DbTableInfoRow;

@Deprecated
public class Generator {
  private static Random rand = new Random();

  private static int randNum() {
    return rand.nextInt(10000);
  }

  private static String randUrn() {
    return String.format("urn:rand:%d.%d.%d", randNum(), randNum(), randNum());
  }

  // Jobs
  public static Job genJob() {
    return genJob(UUID.randomUUID());
  }

  public static Job genJob(UUID namespaceID) {
    final Instant createdAt = Instant.now();
    final Instant updatedAt = createdAt;
    int jobNum = randNum();
    return new Job(
        UUID.randomUUID(),
        "job" + jobNum,
        "http://foo.bar/" + jobNum,
        namespaceID,
        null,
        Arrays.asList(randUrn(), randUrn()),
        Arrays.asList(randUrn(), randUrn()),
        createdAt,
        updatedAt);
  }

  public static Job cloneJob(Job job) {
    return new Job(
        job.getGuid(),
        job.getName(),
        job.getLocation(),
        job.getNamespaceGuid(),
        job.getDescription(),
        job.getInputDatasetUrns(),
        job.getOutputDatasetUrns(),
        job.getCreatedAt(),
        job.getUpdatedAt());
  }

  // Job Runs
  public static JobRun genJobRun() {
    return new JobRun(
        UUID.randomUUID(),
        JobRunState.State.toInt(JobRunState.State.NEW),
        UUID.randomUUID(),
        "abc123",
        "{'foo': 1}",
        null,
        null,
        null);
  }

  public static JobRun cloneJobRun(JobRun j) {
    return new JobRun(
        j.getGuid(),
        j.getCurrentState(),
        j.getJobVersionGuid(),
        j.getRunArgsHexDigest(),
        j.getRunArgs(),
        null,
        null,
        null);
  }

  // Job Run States
  public static JobRunState genJobRunState() {
    return new JobRunState(
        UUID.randomUUID(), Instant.now(), UUID.randomUUID(), JobRunState.State.NEW);
  }

  public static JobRunState cloneJobRunState(JobRunState jrs) {
    return new JobRunState(
        jrs.getGuid(), jrs.getTransitionedAt(), jrs.getJobRunGuid(), jrs.getState());
  }

  // Job Versions
  public static JobVersion genJobVersion() {
    return new JobVersion(
        UUID.randomUUID(),
        UUID.randomUUID(),
        "http://foo.bar",
        UUID.randomUUID(),
        null,
        null,
        null);
  }

  public static JobVersion genJobVersion(UUID jobID) {
    return new JobVersion(
        UUID.randomUUID(), jobID, "http://foo.bar", UUID.randomUUID(), null, null, null);
  }

  public static JobVersion genJobVersion(Job job) {
    return new JobVersion(
        UUID.randomUUID(), job.getGuid(), job.getLocation(), UUID.randomUUID(), null, null, null);
  }

  public static JobVersion cloneJobVersion(JobVersion jv) {
    return new JobVersion(
        jv.getGuid(),
        jv.getJobGuid(),
        jv.getUri(),
        jv.getVersion(),
        jv.getLatestJobRunGuid(),
        jv.getCreatedAt(),
        jv.getUpdatedAt());
  }

  // Namespaces
  public static Namespace genNamespace() {
    final int nsNum = randNum();
    return new Namespace(
        UUID.randomUUID(), "ns" + nsNum, "ns connectionUrl" + nsNum, "ns desc" + nsNum);
  }

  public static Namespace cloneNamespace(Namespace n) {
    return new Namespace(n.getGuid(), n.getName(), n.getOwner(), n.getDescription());
  }

  // Run Args
  public static RunArgs genRunArgs() {
    return new RunArgs("abc123", "{'foo': 1}", null);
  }

  public static RunArgs cloneRunArgs(RunArgs ra) {
    return new RunArgs(ra.getHexDigest(), ra.getJson(), ra.getCreatedAt());
  }

  // Datasource
  public static Datasource genDatasource() {
    final ConnectionUrl connectionUrl =
        ConnectionUrl.of("jdbc:postgresql://localhost:5431/novelists_" + randNum());
    final DatasourceName datasourceName = DatasourceName.of("postgresqllocalhost" + randNum());
    final DatasourceUrn datasourceUrn = DatasourceUrn.of(connectionUrl, datasourceName);

    return new Datasource(datasourceName, Instant.now(), datasourceUrn, connectionUrl);
  }

  // Data Source Rows
  public static DatasourceRow genDatasourceRow() {
    final int datasourceNum = randNum();
    final String connectionUrl = "jdbc:postgresql://localhost:5431/novelists_" + randNum();
    final String datasourceName = "Datasource" + datasourceNum;
    final String datasourceUrn =
        DatasourceUrn.of(ConnectionUrl.of(connectionUrl), DatasourceName.of(datasourceName))
            .getValue();
    return DatasourceRow.builder()
        .uuid(UUID.randomUUID())
        .urn(datasourceUrn)
        .name(datasourceName)
        .connectionUrl(connectionUrl)
        .createdAt(Instant.now())
        .build();
  }

  // DatasetUrn
  public static DatasetUrn genDatasetUrn() {
    return DatasetUrn.of(genDatasource().getName(), DatasetName.of("dataset" + randNum()));
  }

  // Dataset Rows
  public static DatasetRow genDatasetRow(UUID namespaceID, UUID dataSourceID) {
    int dataSourceNum = randNum();
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .name("DatasetRow" + dataSourceNum)
        .namespaceUuid(namespaceID)
        .datasourceUuid(dataSourceID)
        .description("dataset " + randNum())
        .urn(genDatasetUrn().toString())
        .build();
  }

  // DbTableInfo Rows
  public static DbTableInfoRow genDbTableInfowRow() {
    return DbTableInfoRow.builder()
        .uuid(UUID.randomUUID())
        .db("db" + randNum())
        .dbSchema("schema" + randNum())
        .build();
  }
}
