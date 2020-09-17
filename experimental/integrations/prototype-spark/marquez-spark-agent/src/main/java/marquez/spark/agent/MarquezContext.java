package marquez.spark.agent;

import static marquez.client.models.JobType.BATCH;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import marquez.client.Backend;
import marquez.client.Backends;
import marquez.client.Clients;
import marquez.client.MarquezWriteOnlyClient;
import marquez.client.models.DatasetId;
import marquez.client.models.DatasetMeta;
import marquez.client.models.DbTableMeta;
import marquez.client.models.JobMeta;
import marquez.client.models.RunMeta;

public class MarquezContext {
  private static final Logger logger = LoggerFactory.getLogger(MarquezContext.class);
  private MarquezWriteOnlyClient marquezClient;

  private String jobNamespace;
  private String jobName;
  private String parentRunId;

  private String get(String[] elements, String name, int index) {
    boolean check = elements.length > index + 1 && name.equals(elements[index]);
    if (check) {
      return elements[index + 1];
    } else {
      logger.warn("missing " + name + " in " + Arrays.toString(elements) + " at " + index);
      return "default";
    }
  }

  public MarquezContext(String argument, Backend marquezBackend) {
    logger.info("Init MarquezContext: " + argument);
    String[] elements = argument.split("\\/");
    String version = get(elements, "api", 1);
    if (version.equals("v1")) {
      logger.info("marquez api v1");
    }
    jobNamespace = get(elements, "namespaces", 3);
    jobName = get(elements, "jobs", 5);
    parentRunId = get(elements, "runs", 7);
    logger.info(String.format("/api/%s/namespaces/%s/jobs/%s/runs/%s", version, jobNamespace, jobName, parentRunId));
    marquezClient = Clients.newWriteOnlyClient(Backends.newLoggingBackend());
  }

  public MarquezContext(
      MarquezWriteOnlyClient marquezClient, String jobNamespace, String jobName, String parentRunId) {
    super();
    this.marquezClient = marquezClient;
    this.jobNamespace = jobNamespace;
    this.jobName = jobName;
    this.parentRunId = parentRunId;
  }

  public DatasetId ensureDatasetExists(String input) {
    String protocol;
    try {
      URL inputUrl = new URL(input);
      protocol=inputUrl.getProtocol();
    } catch (MalformedURLException e) {
      protocol = "unknown";
    }
    String namespace = protocol;
    String name = input;
    DatasetMeta meta = DbTableMeta.builder().sourceName(protocol).physicalName(name).build();
    marquezClient.createDataset(namespace, name, meta);
    return new DatasetId(namespace, name);
  }

  public UUID startRun(long jobStartTime, List<String> inputs, List<String> outputs, String extraInfo) {
    Set<DatasetId> inputIds = new HashSet<DatasetId>();
    for (String input : inputs) {
      inputIds.add(ensureDatasetExists(input));

    }
    Set<DatasetId> outputIds = new HashSet<DatasetId>();
    for (String output : outputs) {
      outputIds.add(ensureDatasetExists(output));
    }
    Map<String, String> context = ImmutableMap.of("spark.extraInfo", extraInfo);
    marquezClient.createJob(jobNamespace, jobName, JobMeta.builder().inputs(inputIds).outputs(outputIds).type(BATCH).context(context ).build());
    UUID runId = UUID.randomUUID();
    marquezClient.createRun(jobNamespace, jobName, new RunMeta(runId.toString(), null, null, null));
    marquezClient.markRunAsRunning(runId.toString(), Instant.ofEpochMilli(jobStartTime));
    return runId;
  }

  public void success(UUID runId, int jobId, long jobEndTime, List<String> outputs) {
    for (String output : outputs) {
      updateOutput(output, runId);
    }
    Instant at = Instant.ofEpochMilli(jobEndTime);
    marquezClient.markRunAsCompleted(runId.toString(), at);
  }

  public void failure(UUID runId, int jobId, long jobEndTime, List<String> outputs, Exception error) {
    for (String output : outputs) {
      updateOutput(output, runId);
    }
    Instant at = Instant.ofEpochMilli(jobEndTime);
    error.printStackTrace(System.out);
    marquezClient.markRunAsFailed(runId.toString(), at);
  }

  private void updateOutput(String output, UUID runId) {
    String protocol;
    try {
      URL outputUrl = new URL(output);
      protocol=outputUrl.getProtocol();
    } catch (MalformedURLException e) {
      protocol = "unknown";
    }
    String namespace = protocol;
    String name = output;
    DatasetMeta meta = DbTableMeta.builder().sourceName(protocol).physicalName(name).runId(runId.toString()).build();
    marquezClient.createDataset(namespace, name, meta);
  }

  public void close() {
    try {
      marquezClient.close();
      marquezClient = null;
    } catch (IOException e) {
      e.printStackTrace(System.out);
    }
  }


}
