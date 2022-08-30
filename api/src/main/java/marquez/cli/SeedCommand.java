/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import static marquez.common.Utils.newObjectMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableList;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.openlineage.client.OpenLineage;
import io.openlineage.client.OpenLineageClient;
import io.openlineage.client.transports.HttpTransport;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * A command to seed the HTTP API with source, dataset, and job metadata using <a
 * href="https://openlineage.io">OpenLineage</a>. The {@code seed} command is meant to be used to
 * explore the features of Marquez. For example, lineage graph analysis, dataset lifecycle
 * management, job run history, etc.
 *
 * <p><b>Note:</b> You must specify {@code metadata} using the command-line argument {@code
 * --metadata}. Metadata must be defined as a Json file containing an array of {@code OpenLineage}
 * events.
 *
 * <h2>Usage</h2>
 *
 * For example, to override the {@code url}:
 *
 * <pre>{@code
 * java -jar marquez-api.jar seed --url http://localhost:5000 --metadata metadata.json
 * }</pre>
 *
 * <p>where, {@code metadata.json} contains metadata for run {@code
 * d46e465b-d358-4d32-83d4-df660ff614dd}:
 *
 * <pre>{@code
 * [
 *   {
 *     "eventType": "START",
 *     "eventTime": "2020-02-22T22:42:42.000Z",
 *     "run": {
 *       "runId": "d46e465b-d358-4d32-83d4-df660ff614dd"
 *     },
 *     "job": {
 *       "namespace": "my-namespace",
 *       "name": "my-job"
 *     },
 *     "inputs": [{
 *       "namespace": "my-namespace",
 *       "name": "my-input"
 *     }],
 *     "producer": "https://github.com/OpenLineage/OpenLineage/tree/0.11.0/client/java"
 *   },
 *   {
 *     "eventType": "COMPLETE",
 *     "eventTime": "2020-02-22T22:48:12.000Z",
 *     "run": {
 *       "runId": "d46e465b-d358-4d32-83d4-df660ff614dd"
 *     },
 *     "job": {
 *       "namespace": "my-namespace",
 *       "name": "my-job"
 *     },
 *     "outputs": [{
 *       "namespace": "my-namespace",
 *       "name": "my-output",
 *       "facets": {
 *         "schema": {
 *           "_producer": "https://github.com/OpenLineage/OpenLineage/tree/0.11.0/client/java",
 *           "_schemaURL": "https://openlineage.io/spec/facets/1-0-0/SchemaDatasetFacet.json",
 *           "fields": [
 *             { "name": "a", "type": "VARCHAR"},
 *             { "name": "b", "type": "VARCHAR"}
 *           ]
 *         }
 *       }
 *     }],
 *     "producer": "https://github.com/OpenLineage/OpenLineage/tree/0.11.0/client/java"
 *   }
 * ]
 * }</pre>
 *
 * <p><b>Note:</b> The {@code seed} command requires a running instance of Marquez.
 */
@Slf4j
public final class SeedCommand extends Command {
  /* Default URL for HTTP backend. */
  private static final String DEFAULT_OL_URL = "http://localhost:8080";

  /* Args for seed command. */
  private static final String CMD_ARG_OL_URL = "url";
  private static final String CMD_ARG_OL_METADATA = "metadata";

  /* Define seed command. */
  public SeedCommand() {
    super("seed", "seeds the HTTP API server with metadata");
  }

  /* Configure seed command. */
  @Override
  public void configure(@NonNull final Subparser subparser) {
    subparser
        .addArgument("--url")
        .dest("url")
        .type(String.class)
        .required(false)
        .setDefault(DEFAULT_OL_URL)
        .help("the HTTP API server url");
    subparser
        .addArgument("--metadata")
        .dest("metadata")
        .type(String.class)
        .required(true)
        .help("the path to the metadata file (ex: path/to/metadata.json)");
  }

  @Override
  public void run(@NonNull Bootstrap<?> bootstrap, @NonNull Namespace namespace) {
    final String olUrl = namespace.getString(CMD_ARG_OL_URL);
    final String olMetadata = namespace.getString(CMD_ARG_OL_METADATA);
    // Use HTTP transport.
    final OpenLineageClient olClient =
        OpenLineageClient.builder().transport(HttpTransport.builder().uri(olUrl).build()).build();
    log.info("Connected to '{}'... attempting to seed with metadata!", olUrl);
    // Load, then emit events.
    final ImmutableList<OpenLineage.RunEvent> olEvents = loadMetadata(olMetadata);
    log.info("Emitting '{}' events to: '{}'", olEvents.size(), olUrl);
    int olEventsEmitted = 0; // Keep count of events emitted.
    for (final OpenLineage.RunEvent olEvent : olEvents) {
      olClient.emit(olEvent);
      olEventsEmitted++;
    }
    log.info("Successfully emitted '{}' events!", olEventsEmitted);
  }

  /* Returns {@link OpenLineage.RunEvent}s contained within the provided metadata file. */
  @SneakyThrows
  private ImmutableList<OpenLineage.RunEvent> loadMetadata(@NonNull String olMetadata) {
    log.info("Loading metadata from: '{}'", olMetadata);
    return newObjectMapper()
        .readValue(
            Paths.get(olMetadata).toFile(),
            new TypeReference<ImmutableList<OpenLineage.RunEvent>>() {});
  }
}
