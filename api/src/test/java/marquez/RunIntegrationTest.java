/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.time.Instant;
import java.util.UUID;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.Run;
import marquez.client.models.RunMeta;
import marquez.client.models.RunState;
import marquez.common.Utils;
import marquez.common.models.RunId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("IntegrationTests")
public class RunIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  public void setup() {
    createNamespace(NAMESPACE_NAME);
    createSource(DB_TABLE_SOURCE_NAME);
  }

  @Test
  public void testApp_markAsFailed() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    client.markRunAs(runId, RunState.FAILED);
  }

  @Test
  public void testApp_markAsAborted() {
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);
    String runId = UUID.randomUUID().toString();
    client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().id(runId).build());
    Run run = client.markRunAs(runId, RunState.ABORTED);

    assertThat(run.getId()).isEqualTo(runId);
  }

  @Test
  public void testApp_updateOutputDataset() {
    final DbTableMeta DB_TABLE_META =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .build();

    client.createDataset(NAMESPACE_NAME, "my-output-ds", DB_TABLE_META);
    final JobMeta JOB_META =
        JobMeta.builder()
            .type(JOB_TYPE)
            .inputs(ImmutableSet.of())
            .outputs(NAMESPACE_NAME, "my-output-ds")
            .location(JOB_LOCATION)
            .context(JOB_CONTEXT)
            .description(JOB_DESCRIPTION)
            .build();
    client.createJob(NAMESPACE_NAME, JOB_NAME, JOB_META);

    Run createdRun = client.createRun(NAMESPACE_NAME, JOB_NAME, RunMeta.builder().build());

    final DbTableMeta DB_TABLE_META_UPDATED =
        DbTableMeta.builder()
            .physicalName(DB_TABLE_PHYSICAL_NAME)
            .sourceName(DB_TABLE_SOURCE_NAME)
            .fields(DB_TABLE_FIELDS)
            .tags(DB_TABLE_TAGS)
            .description(DB_TABLE_DESCRIPTION)
            .runId(createdRun.getId())
            .build();
    client.createDataset(NAMESPACE_NAME, "my-output-ds", DB_TABLE_META_UPDATED);

    client.markRunAs(createdRun.getId(), RunState.COMPLETED);
  }

  @Test
  public void testSerialization() throws JsonProcessingException {
    marquez.service.models.Run run =
        new marquez.service.models.Run(
            new RunId(UUID.randomUUID().toString()),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            Instant.now(),
            marquez.common.models.RunState.COMPLETED,
            Instant.now(),
            Instant.now(),
            100L,
            ImmutableMap.of(),
            NAMESPACE_NAME,
            JOB_NAME,
            UUID.randomUUID(),
            "location",
            ImmutableList.of(),
            ImmutableList.of(),
            ImmutableMap.of(),
            ImmutableMap.of());
    ObjectMapper objectMapper = Utils.newObjectMapper();
    String json = objectMapper.writeValueAsString(run);
    marquez.service.models.Run deser =
        objectMapper.readValue(json, marquez.service.models.Run.class);
    assertThat(deser).usingRecursiveComparison().ignoringFields("location").isEqualTo(run);
  }
}
