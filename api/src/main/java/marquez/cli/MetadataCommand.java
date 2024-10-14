/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.COMPLETE;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.FAIL;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.START;
import static java.time.ZoneOffset.UTC;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

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
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

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
@Slf4j
public final class MetadataCommand extends Command {
  /* Used for event randomization. */
  private static final Random RANDOM = new Random();
  private static final ZoneId AMERICA_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private static final List<String> FIELD_TYPES = ImmutableList.of("VARCHAR", "TEXT", "INTEGER");

  /* Used to calculate (approximate) total bytes per event. */
  private static final int BYTES_PER_RUN = 578;
  private static final int BYTES_PER_JOB = 58;
  private static final int BYTES_PER_FIELD_IN_SCHEMA = 256;

  /* Default I/O and schema fields per event. */
  private static final int DEFAULT_NUM_OF_IO_PER_EVENT = 8;
  private static final int DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT = 16;

  /* Default runs. */
  private static final int DEFAULT_RUNS = 25;
  private static final int DEFAULT_MIN_RUN_DURATION = 300; // 5.minutes
  private static final int DEFAULT_MAX_RUN_DURATION = 900; // 15.minutes
  private static final int DEFAULT_RUN_DURATION =
      DEFAULT_MIN_RUN_DURATION
          + RANDOM.nextInt(
              DEFAULT_MAX_RUN_DURATION
                  - DEFAULT_MIN_RUN_DURATION
                  + 1); // Between 5.minutes and 15.minutes
  private static final int DEFAULT_RUNS_PER_JOB = 10;
  private static final int DEFAULT_MAX_RUNS_FAILS_PER_JOB = 2;
  private static final ZonedDateTime DEFAULT_RUNS_START_TIME = newEventTimeAsUtc();
  private static final ZonedDateTime DEFAULT_RUNS_END_TIME =
      DEFAULT_RUNS_START_TIME.plusSeconds(DEFAULT_RUN_DURATION);

  /* Default bytes. */
  private static final int DEFAULT_BYTES_PER_EVENT =
      BYTES_PER_RUN
          + BYTES_PER_JOB
          + ((BYTES_PER_FIELD_IN_SCHEMA * DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT)
              * DEFAULT_NUM_OF_IO_PER_EVENT);

  /* Default output. */
  private static final String DEFAULT_OUTPUT = "metadata.json";

  /* Args for metadata command. */
  private static final String CMD_ARG_METADATA_RUNS = "runs";
  private static final String CMD_ARG_METADATA_RUNS_PER_JOB = "run-per-job";
  private static final String CMD_ARG_METADATA_MAX_RUN_FAILS_PER_JOB = "max-run-fails-per-job";
  private static final String CMD_ARG_METADATA_MIN_RUN_DURATION = "min-run-duration";
  private static final String CMD_ARG_METADATA_MAX_RUN_DURATION = "max-run-duration";
  private static final String CMD_ARG_METADATA_RUN_START_TIME = "run-start-time";
  private static final String CMD_ARG_METADATA_RUN_END_TIME = "run-end-time";
  private static final String CMD_ARG_METADATA_BYTES_PER_EVENT = "bytes-per-event";
  private static final String CMD_ARG_METADATA_OUTPUT = "output";

  private static final String OL_NAMESPACE = newNamespaceName().getValue();
  private static final OpenLineage OL =
      new OpenLineage(
          URI.create(
              "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java"));

  /* Define metadata command. */
  public MetadataCommand() {
    super("metadata", "generate random metadata using the OpenLineage standard");
  }

  /* Configure metadata command. */
  @Override
  public void configure(@NonNull Subparser subparser) {
    subparser
        .addArgument("--runs")
        .dest("runs")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_RUNS)
        .help("limits OL runs up to N");
    subparser
        .addArgument("--runs-per-job")
        .dest("run-per-job")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_RUNS_PER_JOB)
        .help("limits OL run executions per job up to N");
    subparser
        .addArgument("--max-run-fails-per-job")
        .dest("max-run-fails-per-job")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_MAX_RUNS_FAILS_PER_JOB)
        .help("maximum OL run fails per job");
    subparser
        .addArgument("--min-run-duration")
        .dest("min-run-duration")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_MIN_RUN_DURATION)
        .help("minimum OL run duration (in seconds) per execution");
    subparser
        .addArgument("--max-run-duration")
        .dest("max-run-duration")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_MAX_RUN_DURATION)
        .help("maximum OL run duration (in seconds) per execution");
    subparser
        .addArgument("--run-start-time")
        .dest("run-start-time")
        .type(String.class)
        .required(false)
        .setDefault(DEFAULT_RUNS_START_TIME)
        .help(
            "specifies the OL run start time in UTC ISO ('YYYY-MM-DDTHH:MM:SSZ');\n"
                + "used for the initial OL run, with subsequent runs starting relative to the\n"
                + "initial start time.");
    subparser
        .addArgument("--run-end-time")
        .dest("run-end-time")
        .type(String.class)
        .required(false)
        .setDefault(DEFAULT_RUNS_END_TIME)
        .help(
            "specifies the OL run end time in UTC ISO ('YYYY-MM-DDTHH:MM:SSZ');\n"
                + "used for the initial OL run, with subsequent runs ending relative to the\n"
                + "initial end time.");
    subparser
        .addArgument("--bytes-per-event")
        .dest("bytes-per-event")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_BYTES_PER_EVENT)
        .help("size (in bytes) per OL event");
    subparser
        .addArgument("-o", "--output")
        .dest("output")
        .type(String.class)
        .required(false)
        .help("output metadata file")
        .setDefault(DEFAULT_OUTPUT);
  }

  @Override
  public void run(@NonNull Bootstrap<?> bootstrap, @NonNull Namespace namespace) {
    final int runs = namespace.getInt(CMD_ARG_METADATA_RUNS);
    final int runsPerJob = namespace.getInt(CMD_ARG_METADATA_RUNS_PER_JOB);
    final int maxRunFailsPerJob = namespace.getInt(CMD_ARG_METADATA_MAX_RUN_FAILS_PER_JOB);
    final int minRunDurationPerExecution = namespace.getInt(CMD_ARG_METADATA_MIN_RUN_DURATION);
    final int maxRunDurationPerExecution = namespace.getInt(CMD_ARG_METADATA_MAX_RUN_DURATION);
    final ZonedDateTime runStartTime =
        ZonedDateTime.parse(
            namespace.getString(CMD_ARG_METADATA_RUN_START_TIME), ISO_ZONED_DATE_TIME);
    final ZonedDateTime runEndTime =
        ZonedDateTime.parse(
            namespace.getString(CMD_ARG_METADATA_RUN_END_TIME), ISO_ZONED_DATE_TIME);
    final int bytesPerEvent = namespace.getInt(CMD_ARG_METADATA_BYTES_PER_EVENT);
    final String output = namespace.getString(CMD_ARG_METADATA_OUTPUT);

    // Generate, then write events to metadata file.
    writeOlEvents(
        newOlEvents(
            runs,
            runsPerJob,
            maxRunFailsPerJob,
            minRunDurationPerExecution,
            maxRunDurationPerExecution,
            runStartTime,
            runEndTime,
            bytesPerEvent),
        output);
  }

  /** Returns new {@link OpenLineage.RunEvent} objects with random values. */
  private static List<OpenLineage.RunEvent> newOlEvents(
      final int runs,
      final int runsPerJob,
      final int maxRunFailsPerJob,
      final int minRunDurationPerExecution,
      final int maxRunDurationPerExecution,
      @NonNull final ZonedDateTime runStartTime,
      @NonNull final ZonedDateTime runEndTime,
      final int bytesPerEvent) {
    checkArgument(maxRunFailsPerJob <= runsPerJob);
    checkArgument(minRunDurationPerExecution <= maxRunDurationPerExecution);
    checkArgument(runStartTime.isBefore(runEndTime));
    System.out.format(
        "Generating '%d' runs, each COMPLETE/FAIL event will have a size of '~%d' (bytes)...\n",
        runs, bytesPerEvent);
    return Stream.generate(
            () -> {
              final RunAttemptsForJob runsForJob =
                  newRunAttemptsForJobWith(
                      runsPerJob,
                      maxRunFailsPerJob,
                      minRunDurationPerExecution,
                      maxRunDurationPerExecution,
                      runStartTime,
                      runEndTime);
              return runsForJob.attempts().stream()
                  .map(
                      runAttempt ->
                          newOlRunEvents(
                              runsForJob.job(),
                              runAttempt.endState(),
                              runAttempt.startedAt(),
                              runAttempt.endedAt(),
                              bytesPerEvent))
                  .flatMap(runEvents -> Stream.of(runEvents.start(), runEvents.end()));
            })
        .limit(runs)
        .flatMap(runEvents -> runEvents)
        .sorted(Comparator.comparing(OpenLineage.RunEvent::getEventType))
        .collect(toImmutableList());
  }

  /**
   * Returns new {@link RunEvents} objects. A {@link RunEvents} object contains the {@code START}
   * and {@code COMPLETE} event for a given run.
   */
  private static RunEvents newOlRunEvents(
      @NonNull OpenLineage.Job job,
      @NonNull final OpenLineage.RunEvent.EventType runEndState,
      @NonNull final ZonedDateTime runStartTime,
      @NonNull final ZonedDateTime runEndTime,
      final int bytesPerEvent) {
    // (1) Generate run.
    final OpenLineage.Run run = newRun();

    // (2) Generate number of I/O for run.
    int numOfInputs = RANDOM.nextInt(DEFAULT_NUM_OF_IO_PER_EVENT);
    int numOfOutputs = DEFAULT_NUM_OF_IO_PER_EVENT - numOfInputs;

    // (3) Generate number of schema fields per I/O for run.
    final int numOfFieldsInSchemaForInputs =
        RANDOM.nextInt(DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT);
    final int numOfFieldsInSchemaForOutputs =
        DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT - numOfFieldsInSchemaForInputs;

    // (4) Generate an event of N bytes if provided; otherwise use default.
    if (bytesPerEvent > DEFAULT_BYTES_PER_EVENT) {
      // Bytes per event:
      // +------------+-----------+-------------------+
      // |  run meta  |  job meta |      I/O meta     |
      // +------------+-----------+-------------------+
      // |->  578B  <-|->  78B  <-|->(256B x N) x P <-|
      // where, N is number of fields per schema, and P is number of I/O per event.
      //
      // (5) Calculate the total I/O per event to equal the bytes per event.
      final int numOfInputsAndOutputsForEvent =
          (bytesPerEvent - BYTES_PER_RUN - BYTES_PER_JOB)
              / (DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT * BYTES_PER_FIELD_IN_SCHEMA);

      // (6) Update the number of I/O to generate for run based on calculation.
      numOfInputs = RANDOM.nextInt(numOfInputsAndOutputsForEvent);
      numOfOutputs = numOfInputsAndOutputsForEvent - numOfInputs;
    }
    return new RunEvents(
        OL.newRunEventBuilder()
            .eventType(START)
            .eventTime(runStartTime)
            .run(run)
            .job(job)
            .inputs(newInputs(numOfInputs, numOfFieldsInSchemaForInputs))
            .outputs(newOutputs(numOfOutputs, numOfFieldsInSchemaForOutputs))
            .build(),
        OL.newRunEventBuilder()
            .eventType(runEndState)
            .eventTime(runEndTime)
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

  /** Returns a new {@link OpenLineage.Run} object. */
  private static OpenLineage.Run newRun() {
    return OL.newRun(
        newRunId().getValue(),
        OL.newRunFacetsBuilder()
            .nominalTime(
                OL.newNominalTimeRunFacetBuilder()
                    .nominalStartTime(newNominalTime())
                    .nominalEndTime(newNominalTime().plusHours(1))
                    .build())
            .build());
  }

  /** Returns a new {@link OpenLineage.Job} object. */
  static OpenLineage.Job newJob() {
    return OL.newJobBuilder().namespace(OL_NAMESPACE).name(newJobName().getValue()).build();
  }

  /** Returns new {@link OpenLineage.InputDataset} objects. */
  private static List<OpenLineage.InputDataset> newInputs(
      final int numOfInputs, final int numOfFields) {
    return Stream.generate(
            () ->
                OL.newInputDatasetBuilder()
                    .namespace(OL_NAMESPACE)
                    .name(newDatasetName().getValue())
                    .facets(
                        OL.newDatasetFacetsBuilder().schema(newDatasetSchema(numOfFields)).build())
                    .build())
        .limit(numOfInputs)
        .collect(toImmutableList());
  }

  /** Returns new {@link OpenLineage.OutputDataset} objects. */
  static List<OpenLineage.OutputDataset> newOutputs(final int numOfOutputs, final int numOfFields) {
    return Stream.generate(
            () ->
                OL.newOutputDatasetBuilder()
                    .namespace(OL_NAMESPACE)
                    .name(newDatasetName().getValue())
                    .facets(
                        OL.newDatasetFacetsBuilder().schema(newDatasetSchema(numOfFields)).build())
                    .build())
        .limit(numOfOutputs)
        .collect(toImmutableList());
  }

  /** Returns a new {@link OpenLineage.SchemaDatasetFacet} object. */
  private static OpenLineage.SchemaDatasetFacet newDatasetSchema(final int numOfFields) {
    return OL.newSchemaDatasetFacetBuilder().fields(newFields(numOfFields)).build();
  }

  /** Returns new {@link OpenLineage.SchemaDatasetFacetFields} objects. */
  private static List<OpenLineage.SchemaDatasetFacetFields> newFields(final int numOfFields) {
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
  private static String newDescription() {
    return "description" + newId();
  }

  /** Returns a new {@code nominal} time. */
  private static ZonedDateTime newNominalTime() {
    return Instant.now().atZone(AMERICA_LOS_ANGELES);
  }

  /** Returns a new {@code event} time. */
  private static ZonedDateTime newEventTimeAsUtc() {
    return ZonedDateTime.now(UTC);
  }

  private static int newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  private static RunAttemptsForJob newRunAttemptsForJobWith(
      final int runsPerJob,
      final int maxRunFailsPerJob,
      final int minRunDurationPerExecution,
      final int maxRunDurationPerExecution,
      @NonNull ZonedDateTime runStartTime,
      @NonNull ZonedDateTime runEndTime) {
    // (1) Generate COMPLETEs runs for job up to N.
    final List<OpenLineage.RunEvent.EventType> completeOrFail =
        IntStream.range(0, runsPerJob).mapToObj(run -> COMPLETE).collect(Collectors.toList());

    // (2) Randomly assign FAIL runs for job by replacing COMPLETEs.
    RANDOM
        .ints(0, runsPerJob)
        .distinct()
        .limit(maxRunFailsPerJob) // Limit FAILs allowed up to N
        .forEach(run -> completeOrFail.set(run, FAIL));

    // (3) ...
    final ImmutableList.Builder<RunAttempt> runAttempts = ImmutableList.builder();
    for (OpenLineage.RunEvent.EventType runEndState : completeOrFail) {
      int durationToAdd =
          minRunDurationPerExecution
              + RANDOM.nextInt(maxRunDurationPerExecution - minRunDurationPerExecution + 1);
      runAttempts.add(new RunAttempt(runEndState, runStartTime, runEndTime));
      runStartTime = runEndTime;
      runEndTime = runStartTime.plusSeconds(durationToAdd);
    }

    return new RunAttemptsForJob(newJob(), runAttempts.build());
  }

  /** A container class for job run attempt info. */
  record RunAttemptsForJob(@NonNull OpenLineage.Job job, @NonNull List<RunAttempt> attempts) {}

  /** A container class for run attempt info. */
  record RunAttempt(
      OpenLineage.RunEvent.EventType endState,
      @NonNull ZonedDateTime startedAt,
      @NonNull ZonedDateTime endedAt) {}

  /** A container class for run info. */
  record RunEvents(@NonNull OpenLineage.RunEvent start, @NonNull OpenLineage.RunEvent end) {}
}
