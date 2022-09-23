/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/** A command to generate graph that can be used as an event template for load testing tool - k6. */
@Slf4j
public final class GraphCommand extends Command {

  /* Default output. */
  private static final String DEFAULT_OUTPUT = "metadata.json";

  /* Args for graph command. */
  private static final String CMD_ARG_GRAPH_DATASETS = "datasets";
  private static final String CMD_ARG_GRAPH_LEVELS = "levels";
  private static final String CMD_ARG_GRAPH_FIRST_LEVEL = "first-level";
  private static final String CMD_ARG_GRAPH_MAX_INPUTS = "max-inputs";
  private static final String CMD_ARG_GRAPH_OUTPUT = "output";

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
        .setDefault(10)
        .help("how many datasets should graph contain");
    subparser
        .addArgument("--levels")
        .dest("levels")
        .type(Integer.class)
        .required(false)
        .setDefault(5)
        .help("how many levels should graph contain");
    subparser
        .addArgument("--first-level")
        .dest("first-level")
        .type(Integer.class)
        .required(false)
        .setDefault(0)
        .help("Set size of first level. If 0, it will be random.");
    subparser
        .addArgument("--max-inputs")
        .dest("max-inputs")
        .type(Integer.class)
        .required(false)
        .setDefault(7)
        .help("set maximum amount of input datasets any job should contain");
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
    List<JobTemplate> graph;

    final int datasets = namespace.getInt(CMD_ARG_GRAPH_DATASETS);
    final int levels = namespace.getInt(CMD_ARG_GRAPH_LEVELS);
    final int firstLevel = namespace.getInt(CMD_ARG_GRAPH_FIRST_LEVEL);
    final int maxInputCount = namespace.getInt(CMD_ARG_GRAPH_MAX_INPUTS);

    if (levels <= 2) {
      System.out.format("Level count needs to be higher than 2, got '%d'\n", levels);
    }

    LayeredDAGGenerator graphGenerator =
        new LayeredDAGGenerator(datasets, levels, firstLevel, maxInputCount);
    graph = graphGenerator.generateGraph();

    final String output = namespace.getString(CMD_ARG_GRAPH_OUTPUT);
    MetadataUtils.writeOlEvents(graph, output);
  }
}
