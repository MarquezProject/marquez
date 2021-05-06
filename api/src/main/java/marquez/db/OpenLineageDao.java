package marquez.db;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.stream.Collectors.joining;
import static marquez.common.Utils.VERSION_DELIM;
import static marquez.common.Utils.VERSION_JOINER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.FieldType;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunState;
import marquez.common.models.SourceType;
import marquez.db.DatasetFieldDao.DatasetFieldMapping;
import marquez.db.JobVersionDao.BagOfJobVersionInfo;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.RunStateRow;
import marquez.db.models.SourceRow;
import marquez.db.models.UpdateLineageRow;
import marquez.db.models.UpdateLineageRow.DatasetRecord;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.DatasetFacets;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;
import org.slf4j.LoggerFactory;

public interface OpenLineageDao extends BaseDao {
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
          + "VALUES (?, ?, ?, ?, ?, ?, ?)")
  void createLineageEvent(
      String eventType,
      Instant eventTime,
      String runId,
      String jobName,
      String jobNamespace,
      PGobject event,
      String producer);

  @Transaction
  default UpdateLineageRow updateMarquezModel(LineageEvent event, ObjectMapper mapper) {
    UpdateLineageRow updateLineageRow = updateBaseMarquezModel(event, mapper);
    RunState runState = getRunState(event.getEventType());
    if (event.getEventType() != null && runState.isDone()) {
      updateMarquezOnComplete(event, updateLineageRow, runState);
    }
    return updateLineageRow;
  }

  default UpdateLineageRow updateBaseMarquezModel(LineageEvent event, ObjectMapper mapper) {
    NamespaceDao namespaceDao = createNamespaceDao();
    DatasetDao datasetDao = createDatasetDao();
    SourceDao sourceDao = createSourceDao();
    JobDao jobDao = createJobDao();
    JobContextDao jobContextDao = createJobContextDao();
    DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();
    RunDao runDao = createRunDao();
    RunArgsDao runArgsDao = createRunArgsDao();
    RunStateDao runStateDao = createRunStateDao();

    Instant now = event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant();

    UpdateLineageRow bag = new UpdateLineageRow();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(
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

    JobRow job =
        jobDao.upsertJob(
            UUID.randomUUID(),
            getJobType(event.getJob()),
            now,
            namespace.getUuid(),
            namespace.getName(),
            event.getJob().getName(),
            description,
            jobContext.getUuid(),
            location,
            jobDao.toJson(toDatasetId(event.getInputs()), mapper));
    bag.setJob(job);

    Map<String, String> runArgsMap = createRunArgs(event);
    RunArgsRow runArgs =
        runArgsDao.upsertRunArgs(
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
      if (event.getRun().getFacets().getNominalTime().getNominalEndTime() != null) {
        nominalEndTime =
            event
                .getRun()
                .getFacets()
                .getNominalTime()
                .getNominalEndTime()
                .withZoneSameInstant(ZoneId.of("UTC"))
                .toInstant();
      }
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
              null,
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime,
              runStateType,
              now,
              namespace.getName(),
              job.getName(),
              location,
              jobContext.getUuid());
    } else {
      run =
          runDao.upsert(
              runUuid,
              event.getRun().getRunId(),
              now,
              null,
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime,
              namespace.getUuid(),
              namespace.getName(),
              job.getName(),
              location,
              jobContext.getUuid());
    }
    bag.setRun(run);

    if (event.getEventType() != null) {
      RunState runStateType = getRunState(event.getEventType());

      RunStateRow runState =
          runStateDao.upsert(UUID.randomUUID(), now, run.getUuid(), runStateType);
      bag.setRunState(runState);
      if (runStateType.isDone()) {
        runDao.updateEndState(run.getUuid(), now, runState.getUuid());
      } else if (runStateType.isStarting()) {
        runDao.updateStartState(run.getUuid(), now, runState.getUuid());
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
                now,
                runUuid,
                true,
                namespaceDao,
                sourceDao,
                datasetDao,
                datasetVersionDao,
                datasetFieldDao,
                runDao);
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
                now,
                runUuid,
                false,
                namespaceDao,
                sourceDao,
                datasetDao,
                datasetVersionDao,
                datasetFieldDao,
                runDao);
        datasetOutputs.add(record);
      }
    }

    bag.setOutputs(Optional.ofNullable(datasetOutputs));
    return bag;
  }

  default Set<DatasetId> toDatasetId(List<Dataset> datasets) {
    Set<DatasetId> set = new HashSet<>();
    if (datasets == null) {
      return set;
    }
    for (Dataset dataset : datasets) {
      set.add(
          new DatasetId(
              NamespaceName.of(dataset.getNamespace()), DatasetName.of(dataset.getName())));
    }
    return set;
  }

  default void updateMarquezOnComplete(
      LineageEvent event, UpdateLineageRow updateLineageRow, RunState runState) {
    BagOfJobVersionInfo bagOfJobVersionInfo =
        createJobVersionDao()
            .upsertJobVersionOnRunTransition(
                updateLineageRow.getRun().getNamespaceName(),
                updateLineageRow.getRun().getJobName(),
                updateLineageRow.getRun().getUuid(),
                runState,
                event.getEventTime().toInstant());
    updateLineageRow.setJobVersionBag(bagOfJobVersionInfo);
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
    return namespace.replaceAll("[^a-z:/A-Z0-9\\-_.]", "_");
  }

  default JobType getJobType(Job job) {
    return JobType.BATCH;
  }

  default DatasetRecord upsertLineageDataset(
      Dataset ds,
      Instant now,
      UUID runUuid,
      boolean isInput,
      NamespaceDao namespaceDao,
      SourceDao sourceDao,
      DatasetDao datasetDao,
      DatasetVersionDao datasetVersionDao,
      DatasetFieldDao datasetFieldDao,
      RunDao runDao) {
    NamespaceRow dsNamespace =
        namespaceDao.upsertNamespaceRow(
            UUID.randomUUID(), now, ds.getNamespace(), DEFAULT_NAMESPACE_OWNER);

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
      source =
          sourceDao.upsertOrDefault(
              UUID.randomUUID(), getSourceType(ds), now, DEFAULT_SOURCE_NAME, "");
    }

    String dsDescription = null;
    if (ds.getFacets() != null && ds.getFacets().getDocumentation() != null) {
      dsDescription = ds.getFacets().getDocumentation().getDescription();
    }

    NamespaceRow datasetNamespace =
        namespaceDao.upsertNamespaceRow(
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
            datasetNamespace.getName(),
            source.getUuid(),
            source.getName(),
            formatDatasetName(ds.getName()),
            ds.getName(),
            dsDescription);

    List<SchemaField> fields =
        Optional.ofNullable(ds.getFacets())
            .map(DatasetFacets::getSchema)
            .map(SchemaDatasetFacet::getFields)
            .orElse(null);

    DatasetVersionRow datasetVersionRow =
        datasetRow
            .getCurrentVersionUuid()
            .filter(v -> isInput) // only fetch the current version if this is a read
            .flatMap(datasetVersionDao::findRowByUuid)
            // if this is a write _or_ if the dataset has no current version,
            // create a new version
            .orElseGet(
                () -> {
                  UUID versionUuid =
                      version(
                          dsNamespace.getName(),
                          source.getName(),
                          datasetRow.getName(),
                          fields,
                          runUuid);
                  DatasetVersionRow row =
                      datasetVersionDao.upsert(
                          UUID.randomUUID(),
                          now,
                          datasetRow.getUuid(),
                          versionUuid,
                          isInput ? null : runUuid,
                          datasetVersionDao.toPgObjectSchemaFields(fields),
                          dsNamespace.getName(),
                          ds.getName());

                  return row;
                });
    List<DatasetFieldMapping> datasetFieldMappings = new ArrayList<>();
    if (fields != null) {
      for (SchemaField field : fields) {
        DatasetFieldRow datasetFieldRow =
            datasetFieldDao.upsert(
                UUID.randomUUID(),
                now,
                field.getName(),
                toFieldType(field.getType()),
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

    return new DatasetRecord(datasetRow, datasetVersionRow, datasetNamespace);
  }

  default String toFieldType(String type) {
    if (type == null) {
      return null;
    }

    try {
      return FieldType.valueOf(type.toUpperCase()).name();
    } catch (Exception e) {
      LoggerFactory.getLogger(getClass()).warn("Can't handle field of type {}", type.toUpperCase());
      return null;
    }
  }

  default String formatDatasetName(String name) {
    return name;
  }

  default String getSourceType(Dataset ds) {
    return SourceType.of("POSTGRESQL").getValue();
  }

  default DatasetType getDatasetType(Dataset ds) {
    return DatasetType.DB_TABLE;
  }

  default RunState getRunState(String eventType) {
    if (eventType == null) {
      return RunState.RUNNING;
    }
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
        return RunState.RUNNING;
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
                fields == null
                    ? ImmutableList.of()
                    : fields.stream()
                        .map(field -> versionField(field.getName(), field.getType()))
                        .collect(joining(VERSION_DELIM)),
                runId)
            .getBytes(UTF_8);
    return UUID.nameUUIDFromBytes(bytes);
  }

  default String versionField(String fieldName, String type) {
    return VERSION_JOINER.join(fieldName, type);
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
