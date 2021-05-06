package marquez.db;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
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
  static UpdateLineageRow createLineageRow(
      OpenLineageDao dao,
      String jobName,
      String status,
      JobFacet jobFacet,
      List<Dataset> inputs,
      List<Dataset> outputs) {
    NominalTimeRunFacet nominalTimeRunFacet = new NominalTimeRunFacet();
    nominalTimeRunFacet.setNominalStartTime(
        Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS));
    nominalTimeRunFacet.setNominalEndTime(
        nominalTimeRunFacet.getNominalStartTime().plus(1, ChronoUnit.HOURS));

    UpdateLineageRow updateLineageRow =
        dao.updateMarquezModel(
            new LineageEvent(
                status,
                Instant.now().atZone(LOCAL_ZONE),
                new Run(
                    UUID.randomUUID().toString(),
                    new RunFacet(nominalTimeRunFacet, null, ImmutableMap.of())),
                new Job(NAMESPACE, jobName, jobFacet),
                inputs,
                outputs,
                PRODUCER_URL.toString()),
            Utils.getMapper());
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

  static DatasetFacets newDatasetFacet(SchemaField... fields) {
    return DatasetFacets.builder()
        .documentation(
            new DocumentationDatasetFacet(PRODUCER_URL, SCHEMA_URL, "the dataset documentation"))
        .schema(new SchemaDatasetFacet(PRODUCER_URL, SCHEMA_URL, Arrays.asList(fields)))
        .dataSource(
            new DatasourceDatasetFacet(
                PRODUCER_URL, SCHEMA_URL, "the source", "http://thesource.com"))
        .description("the dataset description")
        .additional(EMPTY_MAP)
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
  static List<JobLineage> writeDownstreamLineage(
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
  static class JobLineage {

    UUID id;
    String name;
    Optional<DatasetRecord> input;
    Optional<DatasetRecord> output;
    List<JobLineage> downstreamJobs;
  }

  /** Entity that encapsulates a dataset's consumer jobs and their output dataset names (if any). */
  @Value
  static class DatasetConsumerJob {

    String name;
    int numConsumers;
    Optional<String> outputDatasetName;
  }
}
