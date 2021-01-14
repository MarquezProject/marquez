package marquez.db;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.KV_JOINER;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.SchemaField;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

public interface OpenLineageDao extends SqlObject {
  public String DEFAULT_SOURCE_NAME = "default";
  public String DEFAULT_NAMESPACE_OWNER = "anonymous";

  @CreateSqlObject
  DatasetDao createDatasetDao();

  @CreateSqlObject
  DatasetFieldDao createDatasetFieldDao();

  @CreateSqlObject
  DatasetVersionDao createDatasetVersionDao();

  @CreateSqlObject
  JobContextDao createJobContextDao();

  @CreateSqlObject
  JobDao createJobDao();

  @CreateSqlObject
  JobVersionDao createJobVersionDao();

  @CreateSqlObject
  NamespaceDao createNamespaceDao();

  @CreateSqlObject
  RunDao createRunDao();

  @CreateSqlObject
  RunArgsDao createRunArgsDao();

  @CreateSqlObject
  RunStateDao createRunStateDao();

  @CreateSqlObject
  SourceDao createSourceDao();

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
  default void updateMarquezModel(LineageEvent event) {
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

    NamespaceRow namespace =
        namespaceDao.upsert(
            UUID.randomUUID(), now, event.getJob().getNamespace(), DEFAULT_NAMESPACE_OWNER);

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
    Map<String, String> context = buildJobContext(event);
    JobContextRow jobContext =
        jobContextDao.upsert(
            UUID.randomUUID(), now, Utils.toJson(context), Utils.checksumFor(context));
    String location = null;
    if (event.getJob().getFacets() != null
        && event.getJob().getFacets().getSourceCodeLocation() != null) {
      location = event.getJob().getFacets().getSourceCodeLocation().getUrl();
    }

    JobVersionRow jobVersion =
        jobVersionDao.upsert(
            UUID.randomUUID(),
            now,
            job.getUuid(),
            jobContext.getUuid(),
            location,
            buildJobVersion(event, context));

    Map<String, String> runArgsMap = createRunArgs(event);
    RunArgsRow runArgs =
        runArgsDao.upsert(
            UUID.randomUUID(), now, Utils.toJson(runArgsMap), Utils.checksumFor(runArgsMap));

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
              now,
              jobVersion.getUuid(),
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime);
    }

    jobVersionDao.updateLatestRun(jobVersion.getUuid(), now, run.getUuid());

    if (event.getEventType() != null) {
      RunState runStateType = getRunState(event.getEventType());

      RunStateRow runState =
          runStateDao.upsert(UUID.randomUUID(), now, run.getUuid(), runStateType);
      if (runStateType.isDone()) {
        runDao.updateEndState(run.getUuid(), now, runState.getUuid());
      } else if (runStateType.isStarting()) {
        // todo: Verify if repeated running states should cause an update
        runDao.updateStartState(run.getUuid(), now, runState.getUuid());
      }
    }

    if (event.getInputs() != null) {
      for (Dataset ds : event.getInputs()) {
        upsertLineageDataset(
            ds,
            jobVersion,
            namespace,
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
      }
    }

    if (event.getOutputs() != null) {
      for (Dataset ds : event.getOutputs()) {
        upsertLineageDataset(
            ds,
            jobVersion,
            namespace,
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
      }
    }
  }

  default JobType getJobType(Job job) {
    return JobType.BATCH;
  }

  default void upsertLineageDataset(
      Dataset ds,
      JobVersionRow jobVersion,
      NamespaceRow namespace,
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
              ds.getFacets().getDataSource().getUri());
    } else {
      source = sourceDao.upsert(UUID.randomUUID(), getSourceType(ds), now, DEFAULT_SOURCE_NAME, "");
    }

    String dsDescription = null;
    if (ds.getFacets() != null && ds.getFacets().getDocumentation() != null) {
      dsDescription = ds.getFacets().getDocumentation().getDescription();
    }

    DatasetRow dataset =
        datasetDao.upsert(
            UUID.randomUUID(),
            getDatasetType(ds),
            now,
            namespace.getUuid(),
            source.getUuid(),
            ds.getName(),
            dsDescription);

    List<SchemaField> fields = null;
    if (ds.getFacets() != null && ds.getFacets().getSchema() != null) {
      fields = ds.getFacets().getSchema().getFields();
    }

    DatasetVersionRow dsVersion =
        datasetVersionDao.upsert(
            UUID.randomUUID(),
            now,
            dataset.getUuid(),
            version(dsNamespace.getName(), source.getName(), dataset.getName(), fields, runUuid),
            isInput ? runUuid : null);

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
                dataset.getUuid());
        datasetFieldMappings.add(
            new DatasetFieldMapping(dsVersion.getUuid(), datasetFieldRow.getUuid()));
      }
    }
    datasetFieldDao.updateFieldMapping(datasetFieldMappings);

    if (isInput) {
      runDao.updateInputMapping(runUuid, dsVersion.getUuid());
    }

    jobVersionDao.upsertDatasetIoMapping(
        jobVersion.getUuid(), dataset.getUuid(), isInput ? IoType.INPUT : IoType.OUTPUT);
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
    return UUID.nameUUIDFromBytes(runId.getBytes());
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
