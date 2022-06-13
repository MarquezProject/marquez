/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.validation.Valid;
import lombok.Value;
import marquez.common.Utils;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.DatasetFacets;
import marquez.service.models.LineageEvent.DatasourceDatasetFacet;
import marquez.service.models.LineageEvent.DocumentationDatasetFacet;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.NominalTimeRunFacet;
import marquez.service.models.LineageEvent.Run;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import org.postgresql.util.PGobject;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class LineageTestUtils {

  public static final ZoneId LOCAL_ZONE = ZoneId.of("America/Los_Angeles");
  public static final ImmutableMap<String, Object> EMPTY_MAP = ImmutableMap.of();
  public static final URI PRODUCER_URL = URI.create("http://test.producer/");
  public static final URI SCHEMA_URL = URI.create("http://test.schema/");
  public static final String NAMESPACE = "namespace";

  /**
   * Create an {@link UpdateLineageRow} from the input job details and datasets.
   *
   * @param dao
   * @param jobName
   * @param status
   * @param jobFacet
   * @param inputs
   * @param outputs
   * @return
   */
  public static UpdateLineageRow createLineageRow(
      OpenLineageDao dao,
      String jobName,
      String status,
      JobFacet jobFacet,
      List<Dataset> inputs,
      List<Dataset> outputs) {
    return createLineageRow(dao, jobName, status, jobFacet, inputs, outputs, null);
  }

  /**
   * Create an {@link UpdateLineageRow} from the input job details and datasets.
   *
   * @param dao
   * @param jobName
   * @param status
   * @param jobFacet
   * @param inputs
   * @param outputs
   * @param parentRunFacet
   * @return
   */
  public static UpdateLineageRow createLineageRow(
      OpenLineageDao dao,
      String jobName,
      String status,
      JobFacet jobFacet,
      List<Dataset> inputs,
      List<Dataset> outputs,
      @Valid LineageEvent.ParentRunFacet parentRunFacet) {
    return createLineageRow(
        dao, jobName, status, jobFacet, inputs, outputs, parentRunFacet, ImmutableMap.of());
  }

  /**
   * Create an {@link UpdateLineageRow} from the input job details and datasets.
   *
   * @param dao
   * @param jobName
   * @param status
   * @param jobFacet
   * @param inputs
   * @param outputs
   * @param parentRunFacet
   * @param runFacets
   * @return
   */
  public static UpdateLineageRow createLineageRow(
      OpenLineageDao dao,
      String jobName,
      String status,
      JobFacet jobFacet,
      List<Dataset> inputs,
      List<Dataset> outputs,
      @Valid LineageEvent.ParentRunFacet parentRunFacet,
      ImmutableMap<String, Object> runFacets) {
    NominalTimeRunFacet nominalTimeRunFacet = new NominalTimeRunFacet();
    nominalTimeRunFacet.setNominalStartTime(
        Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS));
    nominalTimeRunFacet.setNominalEndTime(
        nominalTimeRunFacet.getNominalStartTime().plus(1, ChronoUnit.HOURS));

    UUID runId = UUID.randomUUID();
    LineageEvent event =
        new LineageEvent(
            status,
            Instant.now().atZone(LOCAL_ZONE),
            new Run(runId.toString(), new RunFacet(nominalTimeRunFacet, parentRunFacet, runFacets)),
            new Job(NAMESPACE, jobName, jobFacet),
            inputs,
            outputs,
            PRODUCER_URL.toString());
    // emulate an OpenLineage RunEvent
    event
        .getProperties()
        .put(
            "_schemaURL",
            "https://openlineage.io/spec/1-0-1/OpenLineage.json#/definitions/RunEvent");
    UpdateLineageRow updateLineageRow = dao.updateMarquezModel(event, Utils.getMapper());
    PGobject jsonObject = new PGobject();
    jsonObject.setType("json");
    try {
      jsonObject.setValue(Utils.toJson(event));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    dao.createLineageEvent(
        event.getEventType() == null ? "" : event.getEventType(),
        event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant(),
        runId,
        event.getJob().getName(),
        event.getJob().getNamespace(),
        jsonObject,
        event.getProducer());

    if (status.equals("COMPLETE")) {
      DatasetDao datasetDao = dao.createDatasetDao();
      updateLineageRow
          .getOutputs()
          .ifPresent(
              outs -> {
                outs.forEach(
                    out ->
                        datasetDao.updateVersion(
                            out.getDatasetRow().getUuid(),
                            Instant.now(),
                            out.getDatasetVersionRow().getUuid()));
              });
    }
    return updateLineageRow;
  }

  public static DatasetFacets newDatasetFacet(SchemaField... fields) {
    return newDatasetFacet(EMPTY_MAP, fields);
  }

  public static DatasetFacets newDatasetFacet(Map<String, Object> facets, SchemaField... fields) {
    return DatasetFacets.builder()
        .documentation(
            new DocumentationDatasetFacet(PRODUCER_URL, SCHEMA_URL, "the dataset documentation"))
        .schema(new SchemaDatasetFacet(PRODUCER_URL, SCHEMA_URL, Arrays.asList(fields)))
        .dataSource(
            new DatasourceDatasetFacet(
                PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
        .description("the dataset description")
        .additional(facets)
        .build();
  }

  /**
   * Recursive function which supports writing a lineage graph by supplying an input dataset and a
   * list of {@link DatasetConsumerJob}s. Each consumer may output up to one dataset, which will be
   * consumed by the number of consumers specified by the {@link DatasetConsumerJob#numConsumers}
   * property.
   *
   * @param openLineageDao
   * @param downstream
   * @param jobFacet
   * @param dataset
   * @return
   */
  public static List<JobLineage> writeDownstreamLineage(
      OpenLineageDao openLineageDao,
      List<DatasetConsumerJob> downstream,
      JobFacet jobFacet,
      Dataset dataset) {
    DatasetConsumerJob consumer = downstream.get(0);
    return IntStream.range(0, consumer.getNumConsumers())
        .mapToObj(
            i -> {
              String jobName = consumer.getName() + i + "<-" + dataset.getName();
              Optional<Dataset> outputs =
                  consumer
                      .getOutputDatasetName()
                      .map(
                          dsName ->
                              new Dataset(
                                  NAMESPACE,
                                  dsName + "<-" + jobName,
                                  newDatasetFacet(
                                      new SchemaField("afield", "string", "a string field"),
                                      new SchemaField("anotherField", "string", "a string field"),
                                      new SchemaField("anInteger", "int", "an integer field"))));
              UpdateLineageRow row =
                  createLineageRow(
                      openLineageDao,
                      jobName,
                      "COMPLETE",
                      jobFacet,
                      Collections.singletonList(dataset),
                      outputs.stream().collect(Collectors.toList()));
              List<JobLineage> downstreamLineage =
                  outputs.stream()
                      .flatMap(
                          out -> {
                            if (consumer.numConsumers > 0) {
                              return writeDownstreamLineage(
                                  openLineageDao,
                                  downstream.subList(1, downstream.size()),
                                  jobFacet,
                                  out)
                                  .stream();
                            } else {
                              return Stream.empty();
                            }
                          })
                      .collect(Collectors.toList());
              return new JobLineage(
                  row.getJob().getUuid(),
                  row.getRun().getUuid(),
                  row.getJob().getName(),
                  row.getInputs().filter(l -> !l.isEmpty()).map(l -> l.get(0)),
                  row.getOutputs().filter(l -> !l.isEmpty()).map(l -> l.get(0)),
                  downstreamLineage);
            })
        .collect(Collectors.toList());
  }

  /**
   * Entity that encapsulates an existing job lineage- a job id, name, its input and output dataset
   * (if any) and a list of downstream jobs.
   */
  @Value
  public static class JobLineage {

    UUID id;
    UUID runId;
    String name;
    Optional<DatasetRecord> input;
    Optional<DatasetRecord> output;
    List<JobLineage> downstreamJobs;
  }

  /** Entity that encapsulates a dataset's consumer jobs and their output dataset names (if any). */
  @Value
  public static class DatasetConsumerJob {

    String name;
    int numConsumers;
    Optional<String> outputDatasetName;
  }
}
