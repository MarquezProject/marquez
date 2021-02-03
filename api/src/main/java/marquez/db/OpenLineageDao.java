package marquez.db;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.KV_JOINER;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.util.Strings;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import marquez.common.Utils;
import marquez.common.models.DatasetType;
import marquez.common.models.JobType;
import marquez.common.models.RunState;
import marquez.common.models.SourceType;
import marquez.db.DatasetFieldDao.DatasetFieldMapping;
import marquez.db.JobVersionDao.IoType;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.RunStateRow;
import marquez.db.models.SourceRow;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.SchemaField;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

public interface OpenLineageDao extends MarquezDao {
  public String DEFAULT_SOURCE_NAME = "default";
  public String DEFAULT_NAMESPACE_OWNER = "anonymous";

  @SqlUpdate(
      "INSERT INTO lineage_events ("
          + "event_type, "
          + "event_time, "
          + "run_id, "
          + "job_name, "
          + "job_namespace, "
          + "event, "
          + "producer) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?) "
          + "ON CONFLICT ON CONSTRAINT lineage_event_pk DO NOTHING")
  void createLineageEvent(
      String eventType,
      Instant eventTime,
      String runId,
      String jobName,
      String jobNamespace,
      PGobject event,
      String producer);

  @Transaction
  default UpdateLineageRow updateMarquezModel(LineageEvent event) {
    NamespaceDao namespaceDao = createNamespaceDao();
    DatasetDao datasetDao = createDatasetDao();
    SourceDao sourceDao = createSourceDao();
    JobDao jobDao = createJobDao();
    JobContextDao jobContextDao = createJobContextDao();
    JobVersionDao jobVersionDao = createJobVersionDao();
    DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();
    RunDao runDao = createRunDao();
    RunArgsDao runArgsDao = createRunArgsDao();
    RunStateDao runStateDao = createRunStateDao();

    Instant now = event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant();

    UpdateLineageRow bag = new UpdateLineageRow();
    NamespaceRow namespace =
        namespaceDao.upsert(
            UUID.randomUUID(),
            now,
            formatNamespaceName(event.getJob().getNamespace()),
            DEFAULT_NAMESPACE_OWNER);
    bag.setNamespace(namespace);

    String description = null;
    if (event.getJob().getFacets() != null
        && event.getJob().getFacets().getDocumentation() != null) {
      description = event.getJob().getFacets().getDocumentation().getDescription();
    }
    JobRow job =
        jobDao.upsert(
            UUID.randomUUID(),
            getJobType(event.getJob()),
            now,
            namespace.getUuid(),
            event.getJob().getName(),
            description);
    bag.setJob(job);

    Map<String, String> context = buildJobContext(event);
    JobContextRow jobContext =
        jobContextDao.upsert(
            UUID.randomUUID(), now, Utils.toJson(context), Utils.checksumFor(context));
    bag.setJobContext(jobContext);
    String location = null;
    if (event.getJob().getFacets() != null
        && event.getJob().getFacets().getSourceCodeLocation() != null) {
      location = getUrlOrPlaceholder(event.getJob().getFacets().getSourceCodeLocation().getUrl());
    }

    JobVersionRow jobVersion =
        jobVersionDao.upsert(
            UUID.randomUUID(),
            now,
            job.getUuid(),
            jobContext.getUuid(),
            location,
            buildJobVersion(event, context));
    bag.setJobVersion(jobVersion);

    jobDao.updateVersion(job.getUuid(), Instant.now(), jobVersion.getUuid());

    Map<String, String> runArgsMap = createRunArgs(event);
    RunArgsRow runArgs =
        runArgsDao.upsert(
            UUID.randomUUID(), now, Utils.toJson(runArgsMap), Utils.checksumFor(runArgsMap));
    bag.setRunArgs(runArgs);

    Instant nominalStartTime = null;
    Instant nominalEndTime = null;
    if (event.getRun().getFacets() != null && event.getRun().getFacets().getNominalTime() != null) {
      nominalStartTime =
          event
              .getRun()
              .getFacets()
              .getNominalTime()
              .getNominalStartTime()
              .withZoneSameInstant(ZoneId.of("UTC"))
              .toInstant();
      nominalEndTime =
          event
              .getRun()
              .getFacets()
              .getNominalTime()
              .getNominalEndTime()
              .withZoneSameInstant(ZoneId.of("UTC"))
              .toInstant();
    }

    UUID runUuid = runToUuid(event.getRun().getRunId());

    RunRow run;
    if (event.getEventType() != null) {
      RunState runStateType = getRunState(event.getEventType());
      run =
          runDao.upsert(
              runUuid,
              event.getRun().getRunId(),
              now,
              jobVersion.getUuid(),
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime,
              runStateType);
    } else {
      run =
          runDao.upsert(
              runUuid,
              event.getRun().getRunId(),
              now,
              jobVersion.getUuid(),
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime);
    }
    bag.setRun(run);

    jobVersionDao.updateLatestRun(jobVersion.getUuid(), now, run.getUuid());

    if (event.getEventType() != null) {
      RunState runStateType = getRunState(event.getEventType());

      RunStateRow runState =
          runStateDao.upsert(UUID.randomUUID(), now, run.getUuid(), runStateType);
      bag.setRunState(runState);
      if (runStateType.isDone()) {
        runDao.updateEndState(run.getUuid(), now, runState.getUuid());
      } else if (runStateType.isStarting()) {
        runDao.upsertStartState(run.getUuid(), now, runState.getUuid());
      }
    }

    // RunInput list uses null as a sentinel value
    List<DatasetRecord> datasetInputs = null;
    if (event.getInputs() != null) {
      datasetInputs = new ArrayList<>();
      for (Dataset ds : event.getInputs()) {
        DatasetRecord record =
            upsertLineageDataset(
                ds,
                jobVersion,
                now,
                runUuid,
                true,
                namespaceDao,
                sourceDao,
                datasetDao,
                datasetVersionDao,
                datasetFieldDao,
                runDao,
                jobVersionDao);
        datasetInputs.add(record);
      }
    }
    bag.setInputs(Optional.ofNullable(datasetInputs));
    // RunInput list uses null as a sentinel value
    List<DatasetRecord> datasetOutputs = null;
    if (event.getOutputs() != null) {
      datasetOutputs = new ArrayList<>();
      for (Dataset ds : event.getOutputs()) {
        DatasetRecord record =
            upsertLineageDataset(
                ds,
                jobVersion,
                now,
                runUuid,
                false,
                namespaceDao,
                sourceDao,
                datasetDao,
                datasetVersionDao,
                datasetFieldDao,
                runDao,
                jobVersionDao);
        datasetOutputs.add(record);
      }
    }

    bag.setOutputs(Optional.ofNullable(datasetOutputs));
    return bag;
  }

  default String getUrlOrPlaceholder(String uri) {
    try {
      return new URI(uri).toASCIIString();
    } catch (URISyntaxException e) {
      try {
        // assume host as string
        return new URI("http://" + uri).toASCIIString();
      } catch (Exception ex) {
        return ""; // empty string for placeholder
      }
    }
  }

  default String formatNamespaceName(String namespace) {
    return namespace.replaceAll("[^a-zA-Z0-9\\-_.]", "_");
  }

  default JobType getJobType(Job job) {
    return JobType.BATCH;
  }

  default DatasetRecord upsertLineageDataset(
      Dataset ds,
      JobVersionRow jobVersion,
      Instant now,
      UUID runUuid,
      boolean isInput,
      NamespaceDao namespaceDao,
      SourceDao sourceDao,
      DatasetDao datasetDao,
      DatasetVersionDao datasetVersionDao,
      DatasetFieldDao datasetFieldDao,
      RunDao runDao,
      JobVersionDao jobVersionDao) {
    NamespaceRow dsNamespace =
        namespaceDao.upsert(UUID.randomUUID(), now, ds.getNamespace(), DEFAULT_NAMESPACE_OWNER);

    SourceRow source;
    if (ds.getFacets() != null && ds.getFacets().getDataSource() != null) {
      source =
          sourceDao.upsert(
              UUID.randomUUID(),
              getSourceType(ds),
              now,
              ds.getFacets().getDataSource().getName(),
              getUrlOrPlaceholder(ds.getFacets().getDataSource().getUri()));
    } else {
      source = sourceDao.upsert(UUID.randomUUID(), getSourceType(ds), now, DEFAULT_SOURCE_NAME, "");
    }

    String dsDescription = null;
    if (ds.getFacets() != null && ds.getFacets().getDocumentation() != null) {
      dsDescription = ds.getFacets().getDocumentation().getDescription();
    }

    NamespaceRow datasetNamespace =
        namespaceDao.upsert(
            UUID.randomUUID(),
            now,
            formatNamespaceName(ds.getNamespace()),
            DEFAULT_NAMESPACE_OWNER);

    DatasetRow datasetRow =
        datasetDao.upsert(
            UUID.randomUUID(),
            getDatasetType(ds),
            now,
            datasetNamespace.getUuid(),
            source.getUuid(),
            formatDatasetName(ds.getName()),
            ds.getName(),
            dsDescription);

    List<SchemaField> fields = null;
    if (ds.getFacets() != null && ds.getFacets().getSchema() != null) {
      fields = ds.getFacets().getSchema().getFields();
    }

    UUID datasetVersion =
        version(dsNamespace.getName(), source.getName(), datasetRow.getName(), fields, runUuid);
    DatasetVersionRow datasetVersionRow =
        datasetVersionDao.upsert(
            UUID.randomUUID(), now, datasetRow.getUuid(), datasetVersion, isInput ? null : runUuid);

    datasetDao.updateVersion(datasetRow.getUuid(), now, datasetVersionRow.getUuid());

    List<DatasetFieldMapping> datasetFieldMappings = new ArrayList<>();
    if (fields != null) {
      for (SchemaField field : fields) {
        DatasetFieldRow datasetFieldRow =
            datasetFieldDao.upsert(
                UUID.randomUUID(),
                now,
                field.getName(),
                field.getType(),
                field.getDescription(),
                datasetRow.getUuid());
        datasetFieldMappings.add(
            new DatasetFieldMapping(datasetVersionRow.getUuid(), datasetFieldRow.getUuid()));
      }
    }
    datasetFieldDao.updateFieldMapping(datasetFieldMappings);

    if (isInput) {
      runDao.updateInputMapping(runUuid, datasetVersionRow.getUuid());
    }

    jobVersionDao.upsertDatasetIoMapping(
        jobVersion.getUuid(), datasetRow.getUuid(), isInput ? IoType.INPUT : IoType.OUTPUT);

    return new DatasetRecord(datasetRow, datasetVersionRow, datasetNamespace);
  }

  default String formatDatasetName(String name) {
    // if url, use the host.path as output, else use name
    // Valid URIs:
    // a.a.a
    //  -note, a.a.a is all in 'path' and all other fields are null
    // gs://bucket
    // file:///out.txt
    // file:///
    // file://localhost/out.txt

    try {
      // Construction name by walking the path and append if not null
      URI uri = new URI(name);
      StringJoiner joiner = new StringJoiner(".");
      if (!Strings.isNullOrEmpty(uri.getScheme())) {
        joiner.add(uri.getScheme());
      }
      if (!Strings.isNullOrEmpty(uri.getHost())) {
        joiner.add(uri.getHost());
      }
      if (!Strings.isNullOrEmpty(uri.getPath())) {
        joiner.add(trimLeadingSlash(uri.getPath()));
      }
      String newName = joiner.toString();

      if (newName.isEmpty()) {
        return name;
      }
      return newName;
    } catch (URISyntaxException e) {
      return name;
    }
  }

  default String trimLeadingSlash(String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  default SourceType getSourceType(Dataset ds) {
    return SourceType.POSTGRESQL;
  }

  default DatasetType getDatasetType(Dataset ds) {
    return DatasetType.DB_TABLE;
  }

  default RunState getRunState(String eventType) {
    switch (eventType.toLowerCase()) {
      case "complete":
        return RunState.COMPLETED;
      case "abort":
        return RunState.ABORTED;
      case "fail":
        return RunState.FAILED;
      case "start":
        return RunState.RUNNING;
      default:
        return RunState.NEW;
    }
  }

  default Map<String, String> createRunArgs(LineageEvent event) {
    Map<String, String> args = new LinkedHashMap<>();
    if (event.getRun().getFacets() != null) {
      if (event.getRun().getFacets().getNominalTime() != null) {
        args.put(
            "nominal_start_time",
            event.getRun().getFacets().getNominalTime().getNominalStartTime().toString());
        if (event.getRun().getFacets().getNominalTime().getNominalEndTime() != null) {
          args.put(
              "nominal_end_time",
              event.getRun().getFacets().getNominalTime().getNominalEndTime().toString());
        }
      }
      if (event.getRun().getFacets().getParent() != null) {
        args.put("run_id", event.getRun().getFacets().getParent().getRun().getRunId());
        args.put("name", event.getRun().getFacets().getParent().getJob().getName());
        args.put("namespace", event.getRun().getFacets().getParent().getJob().getNamespace());
      }
    }
    return args;
  }

  default UUID buildJobVersion(LineageEvent event, Map<String, String> context) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                event.getJob().getNamespace(),
                event.getJob().getName(),
                event.getProducer(),
                KV_JOINER.join(context))
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }

  default Map<String, String> buildJobContext(LineageEvent event) {
    Map<String, String> args = new LinkedHashMap<>();
    if (event.getJob().getFacets() != null) {
      if (event.getJob().getFacets().getSourceCodeLocation() != null) {
        if (event.getJob().getFacets().getSourceCodeLocation().getType() != null) {
          args.put(
              "job.facets.sourceCodeLocation.type",
              event.getJob().getFacets().getSourceCodeLocation().getType());
        }
        if (event.getJob().getFacets().getSourceCodeLocation().getUrl() != null) {
          args.put(
              "job.facets.sourceCodeLocation.url",
              event.getJob().getFacets().getSourceCodeLocation().getUrl());
        }
      }
      if (event.getJob().getFacets().getSql() != null) {
        args.put("sql", event.getJob().getFacets().getSql().getQuery());
      }
    }

    return args;
  }

  default UUID runToUuid(String runId) {
    try {
      return UUID.fromString(runId);
    } catch (Exception e) {
      // Allow non-UUID runId
      return UUID.nameUUIDFromBytes(runId.getBytes());
    }
  }

  default UUID version(
      String namespace,
      String sourceName,
      String datasetName,
      List<SchemaField> fields,
      UUID runId) {
    final byte[] bytes =
        VERSION_JOINER
            .join(
                namespace,
                sourceName,
                datasetName,
                sourceName,
                fields == null
                    ? ImmutableList.of()
                    : fields.stream()
                        .map(
                            field ->
                                versionField(
                                    field.getName(), field.getType(), field.getDescription()))
                        .collect(joining(VERSION_DELIM)),
                runId)
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }

  default String versionField(String fieldName, String type, String description) {
    return VERSION_JOINER.join(fieldName, type, description);
  }

  default PGobject createJsonArray(LineageEvent event, ObjectMapper mapper) {
    try {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(mapper.writeValueAsString(event));
      return jsonObject;
    } catch (Exception e) {
      throw new RuntimeException("Could write lineage event to db", e);
    }
  }
}
