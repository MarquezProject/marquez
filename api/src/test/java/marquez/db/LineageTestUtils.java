package marquez.db;

import io.openlineage.client.OpenLineage;
import java.net.URI;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import lombok.Value;
import marquez.common.Utils;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import org.postgresql.util.PGobject;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class LineageTestUtils {

  public static final ZoneId LOCAL_ZONE = ZoneId.of("America/Los_Angeles");
  public static final ImmutableMap<String, OpenLineage.CustomFacet> EMPTY_MAP = ImmutableMap.of();
  public static final URI PRODUCER_URL = URI.create("http://test.producer/");
  public static final URI SCHEMA_URL = URI.create("http://test.schema/");
  public static final String NAMESPACE = "namespace";
  public static final OpenLineage OPEN_LINEAGE = new OpenLineage(PRODUCER_URL);

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
      OpenLineage.JobFacets jobFacet,
      List<OpenLineage.InputDataset> inputs,
      List<OpenLineage.OutputDataset> outputs) {
    ZonedDateTime now = Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS);
    OpenLineage.NominalTimeRunFacet nominalTimeRunFacet =
        OPEN_LINEAGE.newNominalTimeRunFacet(now, now.plusHours(1));

    UUID runId = UUID.randomUUID();
    OpenLineage.RunEvent event =
        OPEN_LINEAGE
            .newRunEventBuilder()
            .eventTime(Instant.now().atZone(LOCAL_ZONE))
            .eventType(status)
            .job(OPEN_LINEAGE.newJob(NAMESPACE, jobName, jobFacet))
            .run(OPEN_LINEAGE.newRun(runId, OPEN_LINEAGE.newRunFacets(nominalTimeRunFacet, null)))
            .inputs(inputs)
            .outputs(outputs)
            .build();

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
        event.getProducer().toString());

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

  public static OpenLineage.DatasetFacets newDatasetFacet(
      OpenLineage.SchemaDatasetFacetFields... fields) {
    return newDatasetFacet(EMPTY_MAP, fields);
  }

  public static OpenLineage.DatasetFacets newDatasetFacet(
      Map<String, OpenLineage.CustomFacet> facets, OpenLineage.SchemaDatasetFacetFields... fields) {
    OpenLineage.DatasetFacetsBuilder builder =
        OPEN_LINEAGE
            .newDatasetFacetsBuilder()
            .documentation(OPEN_LINEAGE.newDocumentationDatasetFacet("the dataset documentation"))
            .schema(OPEN_LINEAGE.newSchemaDatasetFacet(Arrays.asList(fields)))
            .dataSource(
                OPEN_LINEAGE.newDatasourceDatasetFacet(
                    "the source", URI.create("http://thesource.com")));
    facets.forEach(builder::put);
    return builder.build();
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
      OpenLineage.JobFacets jobFacet,
      OpenLineage.Dataset dataset) {
    DatasetConsumerJob consumer = downstream.get(0);
    return IntStream.range(0, consumer.getNumConsumers())
        .mapToObj(
            i -> {
              String jobName = consumer.getName() + i + "<-" + dataset.getName();
              Optional<OpenLineage.OutputDataset> outputs =
                  consumer
                      .getOutputDatasetName()
                      .map(
                          dsName ->
                              OPEN_LINEAGE.newOutputDataset(
                                  NAMESPACE,
                                  dsName + "<-" + jobName,
                                  newDatasetFacet(
                                      OPEN_LINEAGE.newSchemaDatasetFacetFields(
                                          "afield", "string", "a string field"),
                                      OPEN_LINEAGE.newSchemaDatasetFacetFields(
                                          "anotherField", "string", "a string field"),
                                      OPEN_LINEAGE.newSchemaDatasetFacetFields(
                                          "anInteger", "int", "an integer field")),
                                  null));
              UpdateLineageRow row =
                  createLineageRow(
                      openLineageDao,
                      jobName,
                      "COMPLETE",
                      jobFacet,
                      Collections.singletonList(
                          OPEN_LINEAGE.newInputDataset(
                              dataset.getNamespace(),
                              dataset.getName(),
                              dataset.getFacets(),
                              null)),
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
