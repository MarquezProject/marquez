package marquez.cli;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import io.openlineage.client.OpenLineage;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.FieldName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;

public class MetadataUtils {
  /* Used for event randomization. */
  private static final Random RANDOM = new Random();
  private static final ZoneId AMERICA_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private static final List<String> FIELD_TYPES = ImmutableList.of("VARCHAR", "TEXT", "INTEGER");

  private static final String OL_NAMESPACE = newNamespaceName().getValue();
  private static final OpenLineage OL =
      new OpenLineage(
          URI.create(
              "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/JobTemplateConverter.java"));

  /**
   * Returns a new {@link OpenLineage.Run} object. A {@code parent} run will be associated with
   * {@code child} run if {@code hasParentRun} is {@code true}; otherwise, the {@code child} run
   * will not have a {@code parent} run.
   */
  public static OpenLineage.Run newRun(final JobTemplate job) {
    return OL.newRun(
        newRunId().getValue(),
        OL.newRunFacetsBuilder()
            .parent(
                job.parentJob().isPresent()
                    ? OL.newParentRunFacetBuilder()
                        .run(OL.newParentRunFacetRun(job.parentJob().get().runId()))
                        .job(newParentJob(job.parentJob().get()))
                        .build()
                    : null)
            .nominalTime(
                OL.newNominalTimeRunFacetBuilder()
                    .nominalStartTime(newNominalTime())
                    .nominalEndTime(newNominalTime().plusHours(1))
                    .build())
            .build());
  }

  /** Returns a new {@link OpenLineage.ParentRunFacetRun} object. */
  public static OpenLineage.ParentRunFacetRun newParentRun() {
    return OL.newParentRunFacetRunBuilder().runId(newRunId().getValue()).build();
  }

  /** Returns a new {@link OpenLineage.ParentRunFacetJob} object. */
  public static OpenLineage.ParentRunFacetJob newParentJob(JobTemplate.ParentRun parentRun) {
    return OL.newParentRunFacetJobBuilder()
        .namespace(parentRun.jobNamespace())
        .name(parentRun.jobName())
        .build();
  }

  /** Returns a new {@link OpenLineage.Job} object. */
  public static OpenLineage.Job newJob(JobTemplate jobTemplate) {
    return OL.newJobBuilder().namespace(OL_NAMESPACE).name(jobTemplate.getJobName()).build();
  }

  /** Returns new {@link OpenLineage.InputDataset} objects. */
  public static List<OpenLineage.InputDataset> newInputs(
      final List<String> inputDatasetNames,
      List<OpenLineage.SchemaDatasetFacet> schemaDatasetFacets) {
    return IntStream.range(0, inputDatasetNames.size())
        .boxed()
        .map(
            i ->
                OL.newInputDatasetBuilder()
                    .namespace(OL_NAMESPACE)
                    .name(inputDatasetNames.get(i))
                    .facets(OL.newDatasetFacetsBuilder().schema(schemaDatasetFacets.get(i)).build())
                    .build())
        .collect(toImmutableList());
  }

  /** Returns new {@link OpenLineage.OutputDataset} objects. */
  public static List<OpenLineage.OutputDataset> newOutputs(
      final List<String> outputDatasetNames,
      List<OpenLineage.SchemaDatasetFacet> schemaDatasetFacets) {
    return IntStream.range(0, outputDatasetNames.size())
        .boxed()
        .map(
            i ->
                OL.newOutputDatasetBuilder()
                    .namespace(OL_NAMESPACE)
                    .name(outputDatasetNames.get(i))
                    .facets(OL.newDatasetFacetsBuilder().schema(schemaDatasetFacets.get(i)).build())
                    .build())
        .collect(toImmutableList());
  }

  /** Returns a new {@link OpenLineage.SchemaDatasetFacet} object. */
  public static OpenLineage.SchemaDatasetFacet newDatasetSchema(final int numOfFields) {
    return OL.newSchemaDatasetFacetBuilder().fields(newFields(numOfFields)).build();
  }

  /** Returns new {@link OpenLineage.SchemaDatasetFacetFields} objects. */
  public static List<OpenLineage.SchemaDatasetFacetFields> newFields(final int numOfFields) {
    return Stream.generate(
            () ->
                OL.newSchemaDatasetFacetFieldsBuilder()
                    .name(newFieldName().getValue())
                    .type(newFieldType())
                    .description(newDescription())
                    .build())
        .limit(numOfFields)
        .collect(toImmutableList());
  }

  /** Returns a new {@link NamespaceName} object. */
  public static NamespaceName newNamespaceName() {
    return NamespaceName.of("namespace" + newId());
  }

  /** Returns a new {@link RunId} object. */
  public static RunId newRunId() {
    return RunId.of(UUID.randomUUID());
  }

  /** Returns a new {@link FieldName} object. */
  public static FieldName newFieldName() {
    return FieldName.of("field" + newId());
  }

  /** Returns a new field {@code type}. */
  public static String newFieldType() {
    return FIELD_TYPES.get(RANDOM.nextInt(FIELD_TYPES.size()));
  }

  /** Returns a new {@code description}. */
  public static String newDescription() {
    return "description" + newId();
  }

  /** Returns a new {@code nominal} time. */
  public static ZonedDateTime newNominalTime() {
    return Instant.now().atZone(AMERICA_LOS_ANGELES);
  }

  /** Returns a new {@code event} time. */
  public static ZonedDateTime newEventTime() {
    return Instant.now().atZone(AMERICA_LOS_ANGELES);
  }

  public static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  /** Write {@link OpenLineage.RunEvent}s to the specified {@code output}. */
  public static void writeOlEvents(@NonNull final List<?> olEvents, @NonNull final String output) {
    System.out.format("Writing '%d' events to: '%s'\n", olEvents.size(), output);
    FileWriter fileWriter;
    PrintWriter printWriter = null;
    try {
      fileWriter = new FileWriter(output);
      printWriter = new PrintWriter(fileWriter);
      printWriter.write(Utils.toJson(olEvents));
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (printWriter != null) {
        printWriter.close();
      }
    }
  }

  /** A container class for run info. */
  record RunEvents(@NonNull OpenLineage.RunEvent start, @NonNull OpenLineage.RunEvent complete) {}
}
