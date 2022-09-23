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
import java.net.URI;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
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
  /* Used to calculate (approximate) total bytes per event. */
  private static final int BYTES_PER_RUN = 578;
  private static final int BYTES_PER_JOB = 58;
  private static final int BYTES_PER_FIELD_IN_SCHEMA = 256;

  /* Default I/O and schema fields per event. */
  private static final int DEFAULT_NUM_OF_IO_PER_EVENT = 8;
  private static final int DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT = 16;

  /* Default runs. */
  private static final int DEFAULT_RUNS = 25;

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
  private static final String CMD_ARG_METADATA_BYTES_PER_EVENT = "bytes-per-event";
  private static final String CMD_ARG_METADATA_OUTPUT = "output";

  /* Used for event randomization. */
  private static final Random RANDOM = new Random();
  private static final ZoneId AMERICA_LOS_ANGELES = ZoneId.of("America/Los_Angeles");
  private static final List<String> FIELD_TYPES = ImmutableList.of("VARCHAR", "TEXT", "INTEGER");

  private static final String OL_NAMESPACE = MetadataUtils.newNamespaceName().getValue();
  private static final OpenLineage OL =
      new OpenLineage(
          URI.create(
              "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/MetadataCommand.java"));

  private static final SingleJobDAGGenerator GENERATOR = new SingleJobDAGGenerator();

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
        .help("the output metadata file")
        .setDefault(DEFAULT_OUTPUT);
  }

  @Override
  public void run(@NonNull Bootstrap<?> bootstrap, @NonNull Namespace namespace) {
    final int runs = namespace.getInt(CMD_ARG_METADATA_RUNS);
    final int bytesPerEvent = namespace.getInt(CMD_ARG_METADATA_BYTES_PER_EVENT);
    final String output = namespace.getString(CMD_ARG_METADATA_OUTPUT);

    // Generate, then write events to metadata file.
    MetadataUtils.writeOlEvents(newOlEvents(runs, bytesPerEvent), output);
  }

  /** Returns new {@link OpenLineage.RunEvent} objects with random values. */
  private static List<OpenLineage.RunEvent> newOlEvents(
      final int numOfRuns, final int bytesPerEvent) {
    System.out.format(
        "Generating '%d' runs, each COMPLETE event will have a size of '~%d' (bytes)...\n",
        numOfRuns, bytesPerEvent);
    return Stream.generate(() -> newOlRunEvents(bytesPerEvent))
        .limit(numOfRuns)
        .flatMap(runEvents -> Stream.of(runEvents.start(), runEvents.complete()))
        .collect(toImmutableList());
  }

  /**
   * Returns new {@link MetadataUtils.RunEvents} objects. A {@link MetadataUtils.RunEvents} object
   * contains the {@code START} and {@code COMPLETE} event for a given run.
   */
  private static MetadataUtils.RunEvents newOlRunEvents(final int bytesPerEvent) {
    // (1) Generate number of I/O for run.
    int numOfInputs = RANDOM.nextInt(DEFAULT_NUM_OF_IO_PER_EVENT);
    int numOfOutputs = DEFAULT_NUM_OF_IO_PER_EVENT - numOfInputs;

    // (2) Generate number of schema fields per I/O for run.
    final int numOfFieldsInSchemaForInputs =
        RANDOM.nextInt(DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT);
    final int numOfFieldsInSchemaForOutputs =
        DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT - numOfFieldsInSchemaForInputs;

    // (3) Generate an event of N bytes if provided; otherwise use default.
    if (bytesPerEvent > DEFAULT_BYTES_PER_EVENT) {
      // Bytes per event:
      // +------------+-----------+-------------------+
      // |  run meta  |  job meta |      I/O meta     |
      // +------------+-----------+-------------------+
      // |->  578B  <-|->  78B  <-|->(256B x N) x P <-|
      // where, N is number of fields per schema, and P is number of I/O per event.
      //
      // (4) Calculate the total I/O per event to equal the bytes per event.
      final int numOfInputsAndOutputsForEvent =
          (bytesPerEvent - BYTES_PER_RUN - BYTES_PER_JOB)
              / (DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT * BYTES_PER_FIELD_IN_SCHEMA);

      // (5) Update the number of I/O to generate for run based on calculation.
      numOfInputs = RANDOM.nextInt(numOfInputsAndOutputsForEvent);
      numOfOutputs = numOfInputsAndOutputsForEvent - numOfInputs;
    }

    JobTemplate jobTemplate = GENERATOR.generateGraph(numOfInputs, numOfOutputs).get(0);

    // (6) Generate run with an optional parent run, then the job.
    final OpenLineage.Run olRun = MetadataUtils.newRun(jobTemplate);
    final OpenLineage.Job olJob = MetadataUtils.newJob(jobTemplate);

    return new MetadataUtils.RunEvents(
        OL.newRunEventBuilder()
            .eventType(START)
            .eventTime(MetadataUtils.newEventTime())
            .run(olRun)
            .job(olJob)
            .inputs(
                MetadataUtils.newInputs(
                    jobTemplate.getInputDatasetNames(),
                    Stream.generate(
                            () -> MetadataUtils.newDatasetSchema(numOfFieldsInSchemaForInputs))
                        .limit(jobTemplate.getInputDatasetNames().size())
                        .toList()))
            .outputs(
                MetadataUtils.newOutputs(
                    jobTemplate.getOutputDatasetNames(),
                    Stream.generate(
                            () -> MetadataUtils.newDatasetSchema(numOfFieldsInSchemaForOutputs))
                        .limit(jobTemplate.getOutputDatasetNames().size())
                        .toList()))
            .build(),
        OL.newRunEventBuilder()
            .eventType(COMPLETE)
            .eventTime(MetadataUtils.newEventTime())
            .run(olRun)
            .job(olJob)
            .build());
  }
}
