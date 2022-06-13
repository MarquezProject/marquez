/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static java.util.Map.entry;
import static org.apache.http.Consts.UTF_8;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import marquez.client.Utils;
import marquez.client.models.Dataset;
import marquez.client.models.DbTableMeta;
import marquez.client.models.Field;
import marquez.client.models.Job;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class FlowIntegrationTest extends BaseIntegrationTest {

  private static final String DATASET_NAME = "test-same-io-dataset";
  private final CloseableHttpClient http = HttpClientBuilder.create().build();

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
    createSource(STREAM_SOURCE_NAME);
  }

  @Test
  public void testSameInputAndOutputDatasetCreatedViaDatasetApi() throws IOException {

    Dataset dataset = createDataset(null);

    createJob(null);

    Run createdRun = client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());

    Dataset outputDataset = createDataset(createdRun.getId());

    client.markRunAs(createdRun.getId(), RunState.COMPLETED);

    assertInputDatasetVersionDiffersFromOutput(getRunResponse(createdRun));
  }

  @Test
  public void testSameInputAndOutputDatasetWithJobRunIdUpdated() throws IOException {

    Dataset dataset = createDataset(null);

    createJob(null);

    Run createdRun = client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());
    Run runningState = client.markRunAs(createdRun.getId(), RunState.RUNNING);

    createJob(createdRun.getId());
    createDataset(createdRun.getId());

    Run completed = client.markRunAs(createdRun.getId(), RunState.COMPLETED);

    assertInputDatasetVersionDiffersFromOutput(getRunResponse(createdRun));
  }

  @Test
  public void testSameInputAndOutputDatasetWithJobRunIdUpdatedAtTheEnd() throws IOException {

    Dataset dataset = createDataset(null);

    createJob(null);

    Run createdRun = client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());
    Run runningState = client.markRunAs(createdRun.getId(), RunState.RUNNING);
    Run completed = client.markRunAs(createdRun.getId(), RunState.COMPLETED);
    createJob(createdRun.getId());

    assertInputDatasetVersionDiffersFromOutput(getRunResponse(createdRun));
  }

  @Test
  public void testOutputVersionShouldBeOnlyOneCreatedViaJobAndDatasetApi() throws IOException {
    Dataset dataset = createDataset(null);
    createJob(null);
    Run createdRun = client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());
    createJob(createdRun.getId());
    Dataset outputDataset = createDataset(createdRun.getId());
    Map<String, Object> runResponse = getRunResponse(createdRun);
    client.markRunAs(createdRun.getId(), RunState.COMPLETED);

    Map<String, Object> body = getRunResponse(createdRun);
    assertThat(((List<Map<String, String>>) body.get("outputVersions"))).size().isEqualTo(1);
    assertInputDatasetVersionDiffersFromOutput(body);
  }

  private void assertInputDatasetVersionDiffersFromOutput(Map<String, Object> body)
      throws IOException {

    List<Map<String, String>> inputDatasetVersionIds =
        ((List<Map<String, String>>) body.get("inputVersions"));
    assertThat(inputDatasetVersionIds.stream().map(Map::entrySet).collect(Collectors.toList()))
        .allMatch(e -> e.contains(entry("namespace", NAMESPACE_NAME)))
        .allMatch(e -> e.contains(entry("name", DATASET_NAME)));

    List<Map<String, String>> outputDatasetVersionIds =
        ((List<Map<String, String>>) body.get("outputVersions"));
    assertThat(outputDatasetVersionIds.stream().map(Map::entrySet).collect(Collectors.toList()))
        .allMatch(e -> e.contains(entry("namespace", NAMESPACE_NAME)))
        .allMatch(e -> e.contains(entry("name", DATASET_NAME)));

    List<String> inputVersions =
        inputDatasetVersionIds.stream().map(it -> it.get("version")).collect(Collectors.toList());
    List<String> outputVersions =
        outputDatasetVersionIds.stream().map(it -> it.get("version")).collect(Collectors.toList());

    assertThat(Collections.disjoint(inputVersions, outputVersions)).isTrue();
  }

  private List<String> extractVersion(List<Map<String, String>> datasetVersionIds) {
    return datasetVersionIds.stream()
        .flatMap(it -> it.entrySet().stream())
        .filter(e -> e.getKey().equals("version"))
        .map(Map.Entry::getValue)
        .collect(Collectors.toList());
  }

  private Map<String, Object> getRunResponse(Run run) throws IOException {
    final HttpGet request =
        new HttpGet(String.format(baseUrl + "/api/v1/jobs/runs/%s", run.getId()));
    request.addHeader(ACCEPT, APPLICATION_JSON.toString());
    final HttpResponse response = http.execute(request);

    return Utils.fromJson(
        EntityUtils.toString(response.getEntity(), UTF_8), new TypeReference<>() {});
  }

  private Dataset createDataset(String runId) {
    DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(
                ImmutableList.of(
                    new Field(
                        "test_field1",
                        "BOOL",
                        ImmutableSet.of(SENSITIVE.getName()),
                        "test_description")))
            .tags(ImmutableSet.of(PII.getName()))
            .description(DB_TABLE_DESCRIPTION)
            .runId(runId)
            .build();

    return client.createDataset(NAMESPACE_NAME, DATASET_NAME, DB_TABLE_META);
  }

  private Job createJob(String runId) {
    JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(NAMESPACE_NAME, DATASET_NAME)
            .outputs(NAMESPACE_NAME, DATASET_NAME)
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .runId(runId)
            .build();
    return client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
  }
}
