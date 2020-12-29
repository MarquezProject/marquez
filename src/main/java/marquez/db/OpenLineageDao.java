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
import marquez.api.model.LineageEvent;
import marquez.api.model.LineageEvent.LineageDataset;
import marquez.api.model.LineageEvent.LineageJob;
import marquez.api.model.LineageEvent.SchemaField;
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
      "INSERT INTO lineage_event ("
          + "event_type, "
          + "event_time, "
          + "run_id, "
          + "job_name, "
          + "job_namespace, "
          + "inputs, "
          + "outputs, "
          + "producer) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
          + "ON CONFLICT ON CONSTRAINT lineage_event_pk DO "
          + "UPDATE SET "
          + "inputs = excluded.inputs,"
          + "outputs = excluded.outputs,"
          + "producer = excluded.producer")
  void createLineageEvent(
      String eventType,
      Instant eventTime,
      String runId,
      String jobName,
      String jobNamespace,
      PGobject inputs,
      PGobject outputs,
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

    Instant now = event.eventTime.withZoneSameInstant(ZoneId.of("UTC")).toInstant();

    NamespaceRow namespace = namespaceDao.upsert(now, event.job.namespace, DEFAULT_NAMESPACE_OWNER);

    String description = null;
    if (event.job.facets != null && event.job.facets.documentation != null) {
      description = event.job.facets.documentation.description;
    }
    JobRow job =
        jobDao.upsert(getJobType(event.job), now, namespace.getUuid(), event.job.name, description);
    Map<String, String> context = buildJobContext(event);
    JobContextRow jobContext =
        jobContextDao.upsert(now, Utils.toJson(context), Utils.checksumFor(context));
    String location = null;
    if (event.job.facets != null && event.job.facets.sourceCodeLocation != null) {
      location = event.job.facets.sourceCodeLocation.url;
    }

    JobVersionRow jobVersion =
        jobVersionDao.upsert(
            now, job.getUuid(), jobContext.getUuid(), location, buildJobVersion(event, context));

    Map<String, String> runArgsMap = createRunArgs(event);
    RunArgsRow runArgs =
        runArgsDao.upsert(now, Utils.toJson(runArgsMap), Utils.checksumFor(runArgsMap));

    Instant nominalStartTime = null;
    Instant nominalEndTime = null;
    if (event.run.facets != null && event.run.facets.nominalTime != null) {
      nominalStartTime =
          event
              .run
              .facets
              .nominalTime
              .nominalStartTime
              .withZoneSameInstant(ZoneId.of("UTC"))
              .toInstant();
      nominalEndTime =
          event
              .run
              .facets
              .nominalTime
              .nominalEndTime
              .withZoneSameInstant(ZoneId.of("UTC"))
              .toInstant();
    }

    UUID runUuid = runToUuid(event.run.runId);

    RunRow run;
    if (event.eventType != null) {
      RunState runStateType = getRunState(event.eventType);
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

    if (event.eventType != null) {
      RunState runStateType = getRunState(event.eventType);

      RunStateRow runState = runStateDao.upsert(now, run.getUuid(), runStateType);
      if (runStateType.isDone()) {
        runDao.updateEndState(run.getUuid(), now, runState.getUuid());
      } else if (runStateType.isStarting()) {
        // todo: Verify if repeated running states should cause an update
        runDao.updateStartState(run.getUuid(), now, runState.getUuid());
      }
    }

    if (event.inputs != null) {
      for (LineageDataset ds : event.inputs) {
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

    if (event.outputs != null) {
      for (LineageDataset ds : event.outputs) {
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

  default JobType getJobType(LineageJob job) {
    return JobType.BATCH;
  }

  default void upsertLineageDataset(
      LineageDataset ds,
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
    NamespaceRow dsNamespace = namespaceDao.upsert(now, ds.namespace, DEFAULT_NAMESPACE_OWNER);

    SourceRow source;
    if (ds.facets != null && ds.facets.dataSource != null) {
      source =
          sourceDao.upsert(
              getSourceType(ds), now, ds.facets.dataSource.name, ds.facets.dataSource.uri);
    } else {
      source = sourceDao.upsert(getSourceType(ds), now, DEFAULT_SOURCE_NAME, "");
    }

    String dsDescription = null;
    if (ds.facets != null && ds.facets.documentation != null) {
      dsDescription = ds.facets.documentation.description;
    }

    DatasetRow dataset =
        datasetDao.upsert(
            getDatasetType(ds), now, namespace.getUuid(), source.getUuid(), ds.name, dsDescription);

    List<SchemaField> fields = null;
    if (ds.facets != null && ds.facets.schema != null) {
      fields = ds.facets.schema.fields;
    }

    DatasetVersionRow dsVersion =
        datasetVersionDao.upsert(
            now,
            dataset.getUuid(),
            version(dsNamespace.getName(), source.getName(), dataset.getName(), fields, runUuid),
            isInput ? runUuid : null);

    List<DatasetFieldMapping> datasetFieldMappings = new ArrayList<>();
    if (fields != null) {
      for (SchemaField field : fields) {
        DatasetFieldRow datasetFieldRow =
            datasetFieldDao.upsert(
                now, field.name, field.type, field.description, dataset.getUuid());
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

  default SourceType getSourceType(LineageDataset ds) {
    return SourceType.POSTGRESQL;
  }

  default DatasetType getDatasetType(LineageDataset ds) {
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
    if (event.run.facets != null) {
      if (event.run.facets.nominalTime != null) {
        args.put(
            "run.facets.nominalTime.nominalStartTime",
            event.run.facets.nominalTime.nominalStartTime.toString());
        if (event.run.facets.nominalTime.nominalEndTime != null) {
          args.put(
              "run.facets.nominalTime.nominalEndTime",
              event.run.facets.nominalTime.nominalEndTime.toString());
        }
      }
      if (event.run.facets.parent != null) {
        args.put("run.facets.parent.run.runId", event.run.facets.parent.run.runId);
        args.put("run.facets.parent.job.name", event.run.facets.parent.job.name);
        args.put("run.facets.parent.job.namespace", event.run.facets.parent.job.namespace);
      }
    }
    return args;
  }

  default UUID buildJobVersion(LineageEvent event, Map<String, String> context) {
    final byte[] bytes =
        VERSION_JOINER
            .join(event.job.namespace, event.job.name, event.producer, KV_JOINER.join(context))
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }

  default Map<String, String> buildJobContext(LineageEvent event) {
    Map<String, String> args = new LinkedHashMap<>();
    if (event.job.facets != null) {
      if (event.job.facets.sourceCodeLocation != null) {
        if (event.job.facets.sourceCodeLocation.type != null) {
          args.put("job.facets.sourceCodeLocation.type", event.job.facets.sourceCodeLocation.type);
        }
        if (event.job.facets.sourceCodeLocation.url != null) {
          args.put("job.facets.sourceCodeLocation.url", event.job.facets.sourceCodeLocation.url);
        }
      }
      if (event.job.facets.sql != null) {
        args.put("job.facets.sql.query", event.job.facets.sql.query);
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
                        .map(field -> versionField(field.name, field.type, field.description))
                        .collect(joining(VERSION_DELIM)),
                runId)
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }

  default String versionField(String fieldName, String type, String description) {
    return VERSION_JOINER.join(fieldName, type, description);
  }

  default PGobject createJsonArray(List<LineageDataset> dataset, ObjectMapper mapper) {
    try {
      PGobject jsonObject = new PGobject();
      jsonObject.setType("json");
      jsonObject.setValue(mapper.writeValueAsString(dataset));
      return jsonObject;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
