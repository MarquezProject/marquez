/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import static io.openlineage.client.OpenLineage.RunEvent.EventType.COMPLETE;
import static io.openlineage.client.OpenLineage.RunEvent.EventType.START;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.openlineage.client.OpenLineage;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
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
public final class GraphCommand extends Command {

  private static final Random RANDOM = new Random();

  /* Default runs. */
  private static final int DEFAULT_RUNS = 25;

  /* Default output. */
  private static final String DEFAULT_OUTPUT = "metadata.json";

  /* Args for graph command. */
  private static final String CMD_ARG_GRAPH_DATASETS = "datasets";
  private static final String CMD_ARG_GRAPH_LEVELS = "levels";
  private static final String CMD_ARG_GRAPH_RUNS = "runs";
  private static final String CMD_ARG_GRAPH_FILES = "files";
  private static final String CMD_ARG_GRAPH_DIRECTORY = "directory";

  private static final int DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT = 16;

  private static final OpenLineage OL =
      new OpenLineage(
          URI.create(
              "https://github.com/MarquezProject/marquez/blob/main/api/src/main/java/marquez/cli/JobTemplateConverter.java"));

  /* Define metadata command. */
  public GraphCommand() {
    super("graph", "generate random metadata graph using the OpenLineage standard");
  }

  /* Configure metadata command. */
  @Override
  public void configure(@NonNull Subparser subparser) {
    subparser
        .addArgument("--datasets")
        .dest("datasets")
        .type(Integer.class)
        .required(false)
        .setDefault(1000)
        .help("how many datasets should graph contain");
    subparser
        .addArgument("--levels")
        .dest("levels")
        .type(Integer.class)
        .required(false)
        .setDefault(5)
        .help("how many levels should graph contain");
    subparser
        .addArgument("--runs")
        .dest("runs")
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_RUNS)
        .help("limits OL runs up to N");
    subparser
        .addArgument("--files")
        .dest("files")
        .type(Integer.class)
        .required(false)
        .help("the number of files on which output should be split")
        .setDefault(DEFAULT_OUTPUT);
    subparser
        .addArgument("-d", "--directory")
        .dest("directory")
        .type(String.class)
        .required(false)
        .help("the output metadata directory")
        .setDefault(DEFAULT_OUTPUT);
  }

  @Override
  public void run(@NonNull Bootstrap<?> bootstrap, @NonNull Namespace namespace) {
    List<JobTemplate> jobs;

    final int datasets = namespace.getInt(CMD_ARG_GRAPH_DATASETS);
    final int levels = namespace.getInt(CMD_ARG_GRAPH_LEVELS);
    LayeredDAGGenerator graphGenerator = new LayeredDAGGenerator(datasets, levels, 7);
    jobs = graphGenerator.generateGraph();

    int numOfRuns = namespace.getInt(CMD_ARG_GRAPH_RUNS);
    int fileCount = namespace.getInt(CMD_ARG_GRAPH_FILES);
    final String directory = namespace.getString(CMD_ARG_GRAPH_DIRECTORY);

    File file = new File(directory);
    if (!file.exists()) {
      file.mkdirs();
    }

    if (jobs.size() > numOfRuns) {
      numOfRuns = jobs.size();
    }

    Stream<OpenLineage.RunEvent> firstRuns =
        IntStream.range(0, jobs.size())
            .boxed()
            .map(jobIndex -> newOlRunEvents(jobs.get(jobIndex)))
            .flatMap(runEvents -> Stream.of(runEvents.start(), runEvents.complete()));
    int leftoverRuns = numOfRuns - jobs.size();

    List<List<OpenLineage.RunEvent>> eventsFiles = new ArrayList<>();
    for (int i = 0; i < fileCount; i++) {
      eventsFiles.add(new ArrayList<>());
    }

    List<OpenLineage.RunEvent> events =
        Stream.concat(
                firstRuns,
                Stream.generate(() -> newOlRunEvents(jobs.get(RANDOM.nextInt(jobs.size()))))
                    .limit(leftoverRuns)
                    .flatMap(runEvents -> Stream.of(runEvents.start(), runEvents.complete())))
            .toList();

    IntStream.range(0, events.size())
        .boxed()
        .forEach(i -> eventsFiles.get(i % fileCount).add(events.get(i)));

    IntStream.range(0, fileCount)
        .boxed()
        .parallel()
        .forEach(
            i -> {
              String path = String.format("%s/%d.json", directory, i);
              MetadataUtils.writeOlEvents(eventsFiles.get(i), path);
            });
  }

  /**
   * Returns new {@link MetadataUtils.RunEvents} objects. A {@link MetadataUtils.RunEvents} object
   * contains the {@code START} and {@code COMPLETE} event for a given run.
   */
  public static MetadataUtils.RunEvents newOlRunEvents(JobTemplate jobTemplate) {
    // (1) Generate run with an optional parent run, then the job.
    final OpenLineage.Run olRun = MetadataUtils.newRun(jobTemplate);
    final OpenLineage.Job olJob = MetadataUtils.newJob(jobTemplate);

    // (3) Generate number of schema fields per I/O for run.
    final int numOfFieldsInSchemaForInputs =
        RANDOM.nextInt(DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT);
    final int numOfFieldsInSchemaForOutputs =
        DEFAULT_NUM_OF_FIELDS_IN_SCHEMA_PER_EVENT - numOfFieldsInSchemaForInputs;

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
