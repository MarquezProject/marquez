/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.COMPLETE;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.START;

import com.google.common.collect.ImmutableList;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
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
import java.util.stream.Stream;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * A command to generate random source, dataset, and job metadata using <a
 * href="https://openlineage.io">OpenLineage</a>.
 *
 * <h2>Usage</h2>
 *
 * For example, the following command will generate {@code metadata.json} with {@code 10} runs
 * ({@code 20} events in total), where each START event will have a size of {@code ~16384} bytes;
 * events will be written to {@code metadata.json} in the {@code current} directory. You may specify
 * the location of {@code metadata.json} by using the command-line argument {@code --output}.
 *
 * <pre>{@code
 * java -jar marquez-api.jar metadata --runs 10 --bytes-per-event 16384
 * }</pre>
 */
public final class MetadataCommand extends Command {
  /* Used to calculate (approximate) total bytes per event. */
  private static final int BYTES_PER_RUN = 578;
  private static final int BYTES_PER_JOB = 58;
  private static final int BYTES_PER_FIELD_IN_SCHEMA = 256;

  /* Default I/O and schema fields per event. */
  private static final int DEFAULT_INPUTS_PER_EVENT = 4;
  private static final int DEFAULT_OUTPUTS_PER_EVENT = 2;
  private static final int DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT = 16;

  /* Default runs. */
  private static final int DEFAULT_RUNS = 25;

  /* Default bytes per input/output. */
  private static final int DEFAULT_BYTES_PER_INPUT =
      BYTES_PER_FIELD_IN_SCHEMA * DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT;
  private static final int DEFAULT_BYTES_PER_OUTPUT =
      BYTES_PER_FIELD_IN_SCHEMA * DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT;

  /* Default bytes. */
  private static final int DEFAULT_BYTES_PER_EVENT =
      BYTES_PER_RUN
          + BYTES_PER_JOB
          + (DEFAULT_BYTES_PER_INPUT * DEFAULT_INPUTS_PER_EVENT)
          + (DEFAULT_BYTES_PER_OUTPUT * DEFAULT_OUTPUTS_PER_EVENT);

  /* Default output. */
  private static final String DEFAULT_OUTPUT = "metadata.json";

  /* Args for metadata command. */
  private Argument runArg;
  private Argument bytesPerEventArg;
  private Argument inputsPerEventArg;
  private Argument bytesPerInputArg;
  private Argument outputsPerEventArg;
  private Argument bytesPerOutputArg;
  private Argument outputArg;

  /* Used for event randomization. */
  private static final Random RANDOM = new Random();
  private static final ZoneId AMERICA_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private static final List<String> FIELD_TYPES = ImmutableList.of("VARCHAR", "TEXT", "INTEGER");
  private static final long DELAY_IN_MINUTES = 10;

  private static final String OL_NAMESPACE = newNamespaceName().getValue();
  private static final OpenLineage OL =
      new OpenLineage(
          URI.create(
              "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java"));

  /* Define metadata command. */
  public MetadataCommand() {
    super("metadata", "generate random metadata using OpenLineage");
  }

  /* Configure metadata command. */
  @Override
  public void configure(@NonNull Subparser subparser) {
    runArg =
        subparser
            .addArgument("-r", "--runs")
            .dest("runs")
            .type(Integer.class)
            .required(false)
            .setDefault(DEFAULT_RUNS)
            .help("limits runs up to N");
    bytesPerEventArg =
        subparser
            .addArgument("--bytes-per-event")
            .dest("bytes-per-event")
            .type(Integer.class)
            .required(false)
            .setDefault(DEFAULT_BYTES_PER_EVENT)
            .help("size (in bytes) per event");
    inputsPerEventArg =
        subparser
            .addArgument("--inputs-per-event")
            .dest("inputs-per-event")
            .type(Integer.class)
            .required(false)
            .help("limits inputs per event to N")
            .setDefault(DEFAULT_INPUTS_PER_EVENT);
    bytesPerInputArg =
        subparser
            .addArgument("--bytes-per-input")
            .dest("bytes-per-input")
            .type(Integer.class)
            .required(false)
            .setDefault(DEFAULT_BYTES_PER_INPUT)
            .help("size (in bytes) per input");
    outputsPerEventArg =
        subparser
            .addArgument("--outputs-per-event")
            .dest("outputs-per-event")
            .type(Integer.class)
            .required(false)
            .help("limits outputs per event to N")
            .setDefault(DEFAULT_OUTPUTS_PER_EVENT);
    bytesPerOutputArg =
        subparser
            .addArgument("--bytes-per-output")
            .dest("bytes-per-output")
            .type(Integer.class)
            .required(false)
            .setDefault(DEFAULT_BYTES_PER_OUTPUT)
            .help("size (in bytes) per output");
    outputArg =
        subparser
            .addArgument("-o", "--output")
            .dest("output")
            .type(String.class)
            .required(false)
            .help("the output metadata file")
            .setDefault(DEFAULT_OUTPUT);
  }

  @Override
  public void run(@NonNull Bootstrap<?> bootstrap, @NonNull Namespace namespace) {
    final int runs = namespace.getInt(runArg.getDest());
    final int bytesPerEvent = namespace.getInt(bytesPerEventArg.getDest());
    int inputsPerEvent = namespace.getInt(inputsPerEventArg.getDest());
    int bytesPerInput = namespace.getInt(bytesPerInputArg.getDest());
    int outputsPerEvent = namespace.getInt(outputsPerEventArg.getDest());
    int bytesPerOutput = namespace.getInt(bytesPerOutputArg.getDest());
    final String output = namespace.getString(outputArg.getDest());

    // Ensure bytes per event is > total I/O bytes.
    if (bytesPerEvent < (bytesPerInput + bytesPerOutput)) {
      final int bytesPerIo = bytesPerEvent / 2;
      System.out.format(
          "'%d' (bytes) < '%d' (bytes), defaulting to '%d' (bytes) per I/O...\n",
          bytesPerEvent, (bytesPerInput + bytesPerOutput), bytesPerIo);
      bytesPerInput = bytesPerIo;
      bytesPerOutput = bytesPerIo;
    }

    // Generate, then write events to metadata file.
    writeOlEvents(
        newOlEvents(
            runs, bytesPerEvent, inputsPerEvent, bytesPerInput, outputsPerEvent, bytesPerOutput),
        output);
  }

  /** Returns new {@link OpenLineage.RunEvent} objects with random values. */
  private static List<OpenLineage.RunEvent> newOlEvents(
      final int runs,
      final int bytesPerEvent,
      final int inputsPerEvent,
      final int bytesPerInput,
      final int outputsPerEvent,
      final int bytesPerOutput) {
    System.out.format(
        "Generating '%d' runs, each 'COMPLETE' event will "
            + "have '%d' inputs, '%d' outputs, and a total size of '~%d' (bytes)...\n",
        runs, inputsPerEvent, outputsPerEvent, bytesPerEvent);
    return Stream.generate(
            () -> newOlRunEvents(inputsPerEvent, bytesPerInput, outputsPerEvent, bytesPerOutput))
        .limit(runs)
        .flatMap(runEvents -> Stream.of(runEvents.start(), runEvents.complete()))
        .collect(toImmutableList());
  }

  /**
   * Returns new {@link RunEvents} objects. A {@link RunEvents} object contains the {@code START}
   * and {@code COMPLETE} event for a given run.
   */
  private static RunEvents newOlRunEvents(
      final int inputsPerEvent,
      final int bytesPerInput,
      final int outputsPerEvent,
      final int bytesPerOutput) {
    // (1) Generate run with an optional parent run, then the job.
    final OpenLineage.Run run = newRun(hasParentRunOrNot());
    final OpenLineage.Job job = newJob();

    // Bytes per event are calculated using:
    //
    // +------------+-----------+----------------------+
    // |  run meta  |  job meta |   I/O meta           |
    // +------------+-----------+----------------------+
    // |->  578B  <-|->  78B  <-|->(K x M) + (N x P) <-|
    //
    // where, K are the bytes per input, M is number of inputs per event, N are the bytes per
    // output, and P is number of outputs per event.
    //
    // (2) Generate event of N bytes.
    return new RunEvents(
        OL.newRunEventBuilder()
            .eventType(START)
            .eventTime(newEventTime())
            .run(run)
            .job(job)
            .inputs(newInputs(inputsPerEvent, bytesPerInput))
            .outputs(newOutputs(outputsPerEvent, bytesPerOutput))
            .build(),
        OL.newRunEventBuilder()
            .eventType(COMPLETE)
            .eventTime(newEventTime().plusMinutes(newDelayInMinutes()))
            .run(run)
            .job(job)
            .build());
  }

  /** Write {@link OpenLineage.RunEvent}s to the specified {@code output}. */
  private static void writeOlEvents(
      @NonNull final List<OpenLineage.RunEvent> olEvents, @NonNull final String output) {
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

  /**
   * Returns a new {@link OpenLineage.Run} object. A {@code parent} run will be associated with
   * {@code child} run if {@code hasParentRun} is {@code true}; otherwise, the {@code child} run
   * will not have a {@code parent} run.
   */
  private static OpenLineage.Run newRun(final boolean hasParentRun) {
    return OL.newRun(
        newRunId().getValue(),
        OL.newRunFacetsBuilder()
            .parent(
                hasParentRun
                    ? OL.newParentRunFacetBuilder().run(newParentRun()).job(newParentJob()).build()
                    : null)
            .nominalTime(
                OL.newNominalTimeRunFacetBuilder()
                    .nominalStartTime(newNominalTime())
                    .nominalEndTime(newNominalTime().plusHours(1))
                    .build())
            .build());
  }

  /** Returns a new {@link OpenLineage.ParentRunFacetRun} object. */
  private static OpenLineage.ParentRunFacetRun newParentRun() {
    return OL.newParentRunFacetRunBuilder().runId(newRunId().getValue()).build();
  }

  /** Returns a new {@link OpenLineage.ParentRunFacetJob} object. */
  private static OpenLineage.ParentRunFacetJob newParentJob() {
    return OL.newParentRunFacetJobBuilder()
        .namespace(OL_NAMESPACE)
        .name(newJobName().getValue())
        .build();
  }

  /** Returns a new {@link OpenLineage.Job} object. */
  static OpenLineage.Job newJob() {
    return OL.newJobBuilder().namespace(OL_NAMESPACE).name(newJobName().getValue()).build();
  }

  /** Returns new {@link OpenLineage.InputDataset} objects. */
  private static List<OpenLineage.InputDataset> newInputs(
      final int inputsPerEvent, final int bytesPerInput) {
    final int bytesPerSchema = bytesPerInput / inputsPerEvent;
    final int bytesPerField = bytesPerSchema / DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT;
    return Stream.generate(
            () ->
                OL.newInputDatasetBuilder()
                    .namespace(OL_NAMESPACE)
                    .name(newDatasetName().getValue())
                    .facets(
                        OL.newDatasetFacetsBuilder()
                            .schema(newDatasetSchema(bytesPerField))
                            .build())
                    .build())
        .limit(inputsPerEvent)
        .collect(toImmutableList());
  }

  /** Returns new {@link OpenLineage.OutputDataset} objects. */
  static List<OpenLineage.OutputDataset> newOutputs(
      final int outputsPerEvent, final int bytesPerOutput) {
    final int bytesPerSchema = bytesPerOutput / outputsPerEvent;
    final int bytesPerField = bytesPerSchema / DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT;
    return Stream.generate(
            () ->
                OL.newOutputDatasetBuilder()
                    .namespace(OL_NAMESPACE)
                    .name(newDatasetName().getValue())
                    .facets(
                        OL.newDatasetFacetsBuilder()
                            .schema(newDatasetSchema(bytesPerField))
                            .build())
                    .build())
        .limit(outputsPerEvent)
        .collect(toImmutableList());
  }

  /** Returns a new {@link OpenLineage.SchemaDatasetFacet} object. */
  private static OpenLineage.SchemaDatasetFacet newDatasetSchema(final int bytesPerField) {
    return OL.newSchemaDatasetFacetBuilder().fields(newFields(bytesPerField)).build();
  }

  /** Returns new {@link OpenLineage.SchemaDatasetFacetFields} objects. */
  private static List<OpenLineage.SchemaDatasetFacetFields> newFields(final int bytesPerField) {
    final int bytesPerDescription = bytesPerField / DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT;
    return Stream.generate(
            () ->
                OL.newSchemaDatasetFacetFieldsBuilder()
                    .name(newFieldName().getValue())
                    .type(newFieldType())
                    .description(newDescription(bytesPerDescription))
                    .build())
        .limit(DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT)
        .collect(toImmutableList());
  }

  /** Returns a new {@link NamespaceName} object. */
  private static NamespaceName newNamespaceName() {
    return NamespaceName.of("namespace" + newId());
  }

  /** Returns a new {@link RunId} object. */
  private static RunId newRunId() {
    return RunId.of(UUID.randomUUID());
  }

  /** Returns a new {@link DatasetName} object. */
  private static DatasetName newDatasetName() {
    return DatasetName.of("dataset" + newId());
  }

  /** Returns a new {@link FieldName} object. */
  private static FieldName newFieldName() {
    return FieldName.of("field" + newId());
  }

  /** Returns a new field {@code type}. */
  private static String newFieldType() {
    return FIELD_TYPES.get(RANDOM.nextInt(FIELD_TYPES.size()));
  }

  /** Returns a new {@link JobName} object. */
  private static JobName newJobName() {
    return JobName.of("job" + newId());
  }

  /** Returns a new {@code description}. */
  private static String newDescription(final int bytesPerDescription) {
    return RandomStringUtils.randomAscii(bytesPerDescription);
  }

  /** Returns a new {@code nominal} time. */
  private static ZonedDateTime newNominalTime() {
    return Instant.now().atZone(AMERICA_LOS_ANGELES);
  }

  /** Returns a new {@code event} time. */
  private static ZonedDateTime newEventTime() {
    return Instant.now().atZone(AMERICA_LOS_ANGELES);
  }

  private static long newDelayInMinutes() {
    return RANDOM.nextLong(DELAY_IN_MINUTES);
  }

  /** Returns {@code true} if parent run should be generated; {@code false} otherwise. */
  private static boolean hasParentRunOrNot() {
    return RANDOM.nextBoolean();
  }

  private static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  /** A container class for run info. */
  record RunEvents(@NonNull OpenLineage.RunEvent start, @NonNull OpenLineage.RunEvent complete) {}
}
