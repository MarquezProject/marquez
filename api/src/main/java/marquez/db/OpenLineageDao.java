/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
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
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunState;
import marquez.common.models.SourceType;
import marquez.db.DatasetFieldDao.DatasetFieldMapping;
import marquez.db.JobVersionDao.BagOfJobVersionInfo;
import marquez.db.mappers.LineageEventMapper;
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
import marquez.service.models.LineageEvent.DocumentationJobFacet;
import marquez.service.models.LineageEvent.Job;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.LifecycleStateChangeFacet;
import marquez.service.models.LineageEvent.NominalTimeRunFacet;
import marquez.service.models.LineageEvent.ParentRunFacet;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(LineageEventMapper.class)
public interface OpenLineageDao extends BaseDao {
  public String DEFAULT_SOURCE_NAME = "default";
  public String DEFAULT_NAMESPACE_OWNER = "anonymous";

  @SqlUpdate(
      "INSERT INTO lineage_events ("
          + "event_type, "
          + "event_time, "
          + "run_uuid, "
          + "job_name, "
          + "job_namespace, "
          + "event, "
          + "producer) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?)")
  void createLineageEvent(
      String eventType,
      Instant eventTime,
      UUID runUuid,
      String jobName,
      String jobNamespace,
      PGobject event,
      String producer);

  @SqlQuery("SELECT event FROM lineage_events WHERE run_uuid = :runUuid")
  List<LineageEvent> findOlEventsByRunUuid(UUID runUuid);

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

    String description =
        Optional.ofNullable(event.getJob().getFacets())
            .map(JobFacet::getDocumentation)
            .map(DocumentationJobFacet::getDescription)
            .orElse(null);

    Map<String, String> context = buildJobContext(event);
    JobContextRow jobContext =
        jobContextDao.upsert(
            UUID.randomUUID(), now, Utils.toJson(context), Utils.checksumFor(context));
    bag.setJobContext(jobContext);

    String location =
        Optional.ofNullable(event.getJob().getFacets())
            .flatMap(f -> Optional.ofNullable(f.getSourceCodeLocation()))
            .flatMap(s -> Optional.ofNullable(s.getUrl()))
            .orElse(null);

    Instant nominalStartTime =
        Optional.ofNullable(event.getRun().getFacets())
            .flatMap(f -> Optional.ofNullable(f.getNominalTime()))
            .map(NominalTimeRunFacet::getNominalStartTime)
            .map(t -> t.withZoneSameInstant(ZoneId.of("UTC")).toInstant())
            .orElse(null);
    Instant nominalEndTime =
        Optional.ofNullable(event.getRun().getFacets())
            .flatMap(f -> Optional.ofNullable(f.getNominalTime()))
            .map(NominalTimeRunFacet::getNominalEndTime)
            .map(t -> t.withZoneSameInstant(ZoneId.of("UTC")).toInstant())
            .orElse(null);

    Logger log = LoggerFactory.getLogger(OpenLineageDao.class);
    Optional<ParentRunFacet> parentRun =
        Optional.ofNullable(event.getRun())
            .map(LineageEvent.Run::getFacets)
            .map(RunFacet::getParent);

    Optional<UUID> parentUuid = parentRun.map(Utils::findParentRunUuid);
    Optional<JobRow> parentJob =
        parentUuid.map(
            uuid -> {
              try {
                ParentRunFacet facet = parentRun.get(); // facet must be present
                log.debug("Found parent run event {}", facet);
                PGobject inputs = new PGobject();
                inputs.setType("json");
                inputs.setValue("[]");
                JobRow parentJobRow =
                    runDao
                        .findJobRowByRunUuid(uuid)
                        .orElseGet(
                            () -> {
                              JobRow newParentJobRow =
                                  jobDao.upsertJob(
                                      UUID.randomUUID(),
                                      getJobType(event.getJob()),
                                      now,
                                      namespace.getUuid(),
                                      namespace.getName(),
                                      Utils.parseParentJobName(facet.getJob().getName()),
                                      null,
                                      jobContext.getUuid(),
                                      location,
                                      null,
                                      inputs);
                              log.info("Created new parent job record {}", newParentJobRow);

                              RunArgsRow argsRow =
                                  runArgsDao.upsertRunArgs(
                                      UUID.randomUUID(),
                                      now,
                                      "{}",
                                      Utils.checksumFor(ImmutableMap.of()));
                              RunRow newRow =
                                  runDao.upsert(
                                      uuid,
                                      null,
                                      facet.getRun().getRunId(),
                                      now,
                                      newParentJobRow.getUuid(),
                                      null,
                                      argsRow.getUuid(),
                                      nominalStartTime,
                                      nominalEndTime,
                                      Optional.ofNullable(event.getEventType())
                                          .map(this::getRunState)
                                          .orElse(null),
                                      now,
                                      namespace.getName(),
                                      newParentJobRow.getName(),
                                      newParentJobRow.getLocation(),
                                      newParentJobRow.getJobContextUuid().orElse(null));
                              log.info("Created new parent run record {}", newRow);
                              return newParentJobRow;
                            });
                log.debug("Found parent job record {}", parentJobRow);
                return parentJobRow;
              } catch (Exception e) {
                throw new RuntimeException("Unable to insert parent run", e);
              }
            });

    // construct the simple name of the job by removing the parent prefix plus the dot '.' separator
    String jobName =
        parentJob
            .map(
                p -> {
                  if (event.getJob().getName().startsWith(p.getName() + '.')) {
                    return event.getJob().getName().substring(p.getName().length() + 1);
                  } else {
                    return event.getJob().getName();
                  }
                })
            .orElse(event.getJob().getName());
    log.debug(
        "Calculated job name {} from job {} with parent {}",
        jobName,
        event.getJob().getName(),
        parentJob.map(JobRow::getName));
    JobRow job =
        parentJob
            .map(
                parent ->
                    jobDao.upsertJob(
                        UUID.randomUUID(),
                        parent.getUuid(),
                        getJobType(event.getJob()),
                        now,
                        namespace.getUuid(),
                        namespace.getName(),
                        jobName,
                        description,
                        jobContext.getUuid(),
                        location,
                        null,
                        jobDao.toJson(toDatasetId(event.getInputs()), mapper)))
            .orElseGet(
                () ->
                    jobDao.upsertJob(
                        UUID.randomUUID(),
                        getJobType(event.getJob()),
                        now,
                        namespace.getUuid(),
                        namespace.getName(),
                        jobName,
                        description,
                        jobContext.getUuid(),
                        location,
                        null,
                        jobDao.toJson(toDatasetId(event.getInputs()), mapper)));
    bag.setJob(job);

    Map<String, String> runArgsMap = createRunArgs(event);
    RunArgsRow runArgs =
        runArgsDao.upsertRunArgs(
            UUID.randomUUID(), now, Utils.toJson(runArgsMap), Utils.checksumFor(runArgsMap));
    bag.setRunArgs(runArgs);

    UUID runUuid = runToUuid(event.getRun().getRunId());

    RunRow run;
    if (event.getEventType() != null) {
      RunState runStateType = getRunState(event.getEventType());
      run =
          runDao.upsert(
              runUuid,
              parentUuid.orElse(null),
              event.getRun().getRunId(),
              now,
              job.getUuid(),
              null,
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime,
              runStateType,
              now,
              namespace.getName(),
              jobName,
              location,
              jobContext.getUuid());
    } else {
      run =
          runDao.upsert(
              runUuid,
              parentUuid.orElse(null),
              event.getRun().getRunId(),
              now,
              job.getUuid(),
              null,
              runArgs.getUuid(),
              nominalStartTime,
              nominalEndTime,
              namespace.getUuid(),
              namespace.getName(),
              jobName,
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
                updateLineageRow.getJob(),
                updateLineageRow.getRun().getUuid(),
                runState,
                event.getEventTime().toInstant());
    updateLineageRow.setJobVersionBag(bagOfJobVersionInfo);
  }

  default String getUrlOrNull(String uri) {
    try {
      return new URI(uri).toASCIIString();
    } catch (URISyntaxException e) {
      try {
        // assume host as string
        return new URI("http://" + uri).toASCIIString();
      } catch (Exception ex) {
        return null;
      }
    }
  }

  default String formatNamespaceName(String namespace) {
    return namespace.replaceAll("[^a-z:/A-Z0-9\\-_.@+]", "_");
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
              getUrlOrNull(ds.getFacets().getDataSource().getUri()));
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

    String dslifecycleState =
        Optional.ofNullable(ds.getFacets())
            .map(DatasetFacets::getLifecycleStateChange)
            .map(LifecycleStateChangeFacet::getLifecycleStateChange)
            .orElse("");

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
            dsDescription,
            dslifecycleState.equalsIgnoreCase("DROP"));

    List<SchemaField> fields =
        Optional.ofNullable(ds.getFacets())
            .map(DatasetFacets::getSchema)
            .map(SchemaDatasetFacet::getFields)
            .orElse(null);

    final DatasetRow dsRow = datasetRow;
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
                      Utils.newDatasetVersionFor(
                              dsNamespace.getName(),
                              source.getName(),
                              dsRow.getPhysicalName(),
                              dsRow.getName(),
                              dslifecycleState,
                              fields,
                              runUuid)
                          .getValue();
                  DatasetVersionRow row =
                      datasetVersionDao.upsert(
                          UUID.randomUUID(),
                          now,
                          dsRow.getUuid(),
                          versionUuid,
                          isInput ? null : runUuid,
                          datasetVersionDao.toPgObjectSchemaFields(fields),
                          dsNamespace.getName(),
                          ds.getName(),
                          dslifecycleState);
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

      // TODO - this is a short term fix until
      // https://github.com/MarquezProject/marquez/issues/1361
      // is fully thought out
      if (datasetRow.getCurrentVersionUuid().isEmpty()) {
        datasetDao.updateVersion(dsRow.getUuid(), now, datasetVersionRow.getUuid());
        datasetRow = datasetRow.withCurrentVersionUuid(datasetVersionRow.getUuid());
      }
    }

    return new DatasetRecord(datasetRow, datasetVersionRow, datasetNamespace);
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
