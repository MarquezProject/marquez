/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import marquez.db.models.ColumnLineageRow;
import marquez.db.models.DatasetFieldRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasetSymlinkRow;
import marquez.db.models.DatasetVersionRow;
import marquez.db.models.InputFieldData;
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
import marquez.service.models.LineageEvent.Run;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.LineageEvent.SchemaDatasetFacet;
import marquez.service.models.LineageEvent.SchemaField;
import org.apache.commons.lang3.tuple.Pair;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RegisterRowMapper(LineageEventMapper.class)
public interface OpenLineageDao extends BaseDao {
  String DEFAULT_SOURCE_NAME = "default";
  String DEFAULT_NAMESPACE_OWNER = "anonymous";

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
  List<LineageEvent> findLineageEventsByRunUuid(UUID runUuid);

  @SqlQuery(
      """
  SELECT event
  FROM lineage_events le
  WHERE (le.event_time < :before
  AND le.event_time >= :after)
  ORDER BY le.event_time DESC
  LIMIT :limit""")
  List<LineageEvent> getAllLineageEventsDesc(ZonedDateTime before, ZonedDateTime after, int limit);

  @SqlQuery(
      """
  SELECT event
  FROM lineage_events le
  WHERE (le.event_time < :before
  AND le.event_time >= :after)
  ORDER BY le.event_time ASC
  LIMIT :limit""")
  List<LineageEvent> getAllLineageEventsAsc(ZonedDateTime before, ZonedDateTime after, int limit);

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
    DatasetSymlinkDao datasetSymlinkDao = createDatasetSymlinkDao();
    DatasetDao datasetDao = createDatasetDao();
    SourceDao sourceDao = createSourceDao();
    JobDao jobDao = createJobDao();
    JobContextDao jobContextDao = createJobContextDao();
    JobFacetsDao jobFacetsDao = createJobFacetsDao();
    DatasetVersionDao datasetVersionDao = createDatasetVersionDao();
    DatasetFieldDao datasetFieldDao = createDatasetFieldDao();
    DatasetFacetsDao datasetFacetsDao = createDatasetFacetsDao();
    RunDao runDao = createRunDao();
    RunArgsDao runArgsDao = createRunArgsDao();
    RunStateDao runStateDao = createRunStateDao();
    ColumnLineageDao columnLineageDao = createColumnLineageDao();
    RunFacetsDao runFacetsDao = createRunFacetsDao();

    Instant now = event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant();

    UpdateLineageRow bag = new UpdateLineageRow();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(
            UUID.randomUUID(),
            now,
            formatNamespaceName(event.getJob().getNamespace()),
            DEFAULT_NAMESPACE_OWNER);
    bag.setNamespace(namespace);

    Map<String, String> context = buildJobContext(event);
    JobContextRow jobContext =
        jobContextDao.upsert(
            UUID.randomUUID(), now, Utils.toJson(context), Utils.checksumFor(context));
    bag.setJobContext(jobContext);

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

    Optional<ParentRunFacet> parentRun =
        Optional.ofNullable(event.getRun()).map(Run::getFacets).map(RunFacet::getParent);
    Optional<UUID> parentUuid = parentRun.map(Utils::findParentRunUuid);

    JobRow job =
        runDao
            .findJobRowByRunUuid(runToUuid(event.getRun().getRunId()))
            .orElseGet(
                () ->
                    buildJobFromEvent(
                        event,
                        mapper,
                        jobDao,
                        now,
                        namespace,
                        jobContext,
                        nominalStartTime,
                        nominalEndTime,
                        parentRun));

    bag.setJob(job);

    Map<String, String> runArgsMap = createRunArgs(event);
    RunArgsRow runArgs =
        runArgsDao.upsertRunArgs(
            UUID.randomUUID(), now, Utils.toJson(runArgsMap), Utils.checksumFor(runArgsMap));
    bag.setRunArgs(runArgs);

    final UUID runUuid = runToUuid(event.getRun().getRunId());

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
              job.getName(),
              job.getLocation(),
              jobContext.getUuid());
      // Add ...
      Optional.ofNullable(event.getRun().getFacets())
          .ifPresent(
              runFacet ->
                  runFacetsDao.insertRunFacetsFor(
                      runUuid, now, event.getEventType(), event.getRun().getFacets()));
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
              job.getName(),
              job.getLocation(),
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

    // Add ...
    Optional.ofNullable(event.getJob().getFacets())
        .ifPresent(
            jobFacet ->
                jobFacetsDao.insertJobFacetsFor(
                    job.getUuid(), runUuid, now, event.getEventType(), event.getJob().getFacets()));

    // RunInput list uses null as a sentinel value
    List<DatasetRecord> datasetInputs = null;
    if (event.getInputs() != null) {
      datasetInputs = new ArrayList<>();
      for (Dataset dataset : event.getInputs()) {
        DatasetRecord record =
            upsertLineageDataset(
                dataset,
                now,
                runUuid,
                true,
                namespaceDao,
                datasetSymlinkDao,
                sourceDao,
                datasetDao,
                datasetVersionDao,
                datasetFieldDao,
                runDao,
                columnLineageDao);
        datasetInputs.add(record);

        // Facets ...
        Optional.ofNullable(dataset.getFacets())
            .ifPresent(
                facets ->
                    datasetFacetsDao.insertDatasetFacetsFor(
                        record.getDatasetRow().getUuid(),
                        runUuid,
                        now,
                        event.getEventType(),
                        facets));
      }
    }
    bag.setInputs(Optional.ofNullable(datasetInputs));
    // RunInput list uses null as a sentinel value
    List<DatasetRecord> datasetOutputs = null;
    if (event.getOutputs() != null) {
      datasetOutputs = new ArrayList<>();
      for (Dataset dataset : event.getOutputs()) {
        DatasetRecord record =
            upsertLineageDataset(
                dataset,
                now,
                runUuid,
                false,
                namespaceDao,
                datasetSymlinkDao,
                sourceDao,
                datasetDao,
                datasetVersionDao,
                datasetFieldDao,
                runDao,
                columnLineageDao);
        datasetOutputs.add(record);

        // Facets ...
        Optional.ofNullable(dataset.getFacets())
            .ifPresent(
                facets ->
                    datasetFacetsDao.insertDatasetFacetsFor(
                        record.getDatasetRow().getUuid(),
                        runUuid,
                        now,
                        event.getEventType(),
                        facets));
      }
    }

    bag.setOutputs(Optional.ofNullable(datasetOutputs));
    return bag;
  }

  private JobRow buildJobFromEvent(
      LineageEvent event,
      ObjectMapper mapper,
      JobDao jobDao,
      Instant now,
      NamespaceRow namespace,
      JobContextRow jobContext,
      Instant nominalStartTime,
      Instant nominalEndTime,
      Optional<ParentRunFacet> parentRun) {
    Logger log = LoggerFactory.getLogger(OpenLineageDao.class);
    String description =
        Optional.ofNullable(event.getJob().getFacets())
            .map(JobFacet::getDocumentation)
            .map(DocumentationJobFacet::getDescription)
            .orElse(null);

    String location =
        Optional.ofNullable(event.getJob().getFacets())
            .flatMap(f -> Optional.ofNullable(f.getSourceCodeLocation()))
            .flatMap(s -> Optional.ofNullable(s.getUrl()))
            .orElse(null);

    Optional<UUID> parentUuid = parentRun.map(Utils::findParentRunUuid);
    Optional<JobRow> parentJob =
        parentUuid.map(
            uuid ->
                findParentJobRow(
                    event,
                    namespace,
                    jobContext,
                    location,
                    nominalStartTime,
                    nominalEndTime,
                    log,
                    parentRun.get(),
                    uuid));

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
    return parentJob
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
  }

  private JobRow findParentJobRow(
      LineageEvent event,
      NamespaceRow namespace,
      JobContextRow jobContext,
      String location,
      Instant nominalStartTime,
      Instant nominalEndTime,
      Logger log,
      ParentRunFacet facet,
      UUID uuid) {
    try {
      log.debug("Found parent run event {}", facet);
      PGobject inputs = new PGobject();
      inputs.setType("json");
      inputs.setValue("[]");
      JobRow parentJobRow =
          createRunDao()
              .findJobRowByRunUuid(uuid)
              .map(
                  j -> {
                    String parentJobName =
                        facet.getJob().getName().equals(event.getJob().getName())
                            ? Utils.parseParentJobName(facet.getJob().getName())
                            : facet.getJob().getName();
                    if (j.getNamespaceName().equals(facet.getJob().getNamespace())
                        && j.getName().equals(parentJobName)) {
                      return j;
                    } else {
                      // Addresses an Airflow integration bug that generated conflicting run UUIDs
                      // for DAGs that had the same name, but ran in different namespaces.
                      UUID parentRunUuid =
                          Utils.toNameBasedUuid(
                              facet.getJob().getNamespace(), parentJobName, uuid.toString());
                      log.warn(
                          "Parent Run id {} has a different job name '{}.{}' from facet '{}.{}'. "
                              + "Assuming Run UUID conflict and generating a new UUID {}",
                          uuid,
                          j.getNamespaceName(),
                          j.getName(),
                          facet.getJob().getNamespace(),
                          facet.getJob().getName(),
                          parentRunUuid);
                      return createParentJobRunRecord(
                          event,
                          namespace,
                          jobContext,
                          location,
                          nominalStartTime,
                          nominalEndTime,
                          parentRunUuid,
                          facet,
                          inputs);
                    }
                  })
              .orElseGet(
                  () ->
                      createParentJobRunRecord(
                          event,
                          namespace,
                          jobContext,
                          location,
                          nominalStartTime,
                          nominalEndTime,
                          uuid,
                          facet,
                          inputs));
      log.debug("Found parent job record {}", parentJobRow);
      return parentJobRow;
    } catch (Exception e) {
      throw new RuntimeException("Unable to insert parent run", e);
    }
  }

  private JobRow createParentJobRunRecord(
      LineageEvent event,
      NamespaceRow namespace,
      JobContextRow jobContext,
      String location,
      Instant nominalStartTime,
      Instant nominalEndTime,
      UUID uuid,
      ParentRunFacet facet,
      PGobject inputs) {
    Instant now = event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant();
    Logger log = LoggerFactory.getLogger(OpenLineageDao.class);
    String parentJobName =
        facet.getJob().getName().equals(event.getJob().getName())
            ? Utils.parseParentJobName(facet.getJob().getName())
            : facet.getJob().getName();
    JobRow newParentJobRow =
        createJobDao()
            .upsertJob(
                UUID.randomUUID(),
                getJobType(event.getJob()),
                now,
                namespace.getUuid(),
                namespace.getName(),
                parentJobName,
                null,
                jobContext.getUuid(),
                location,
                null,
                inputs);
    log.info("Created new parent job record {}", newParentJobRow);

    RunArgsRow argsRow =
        createRunArgsDao()
            .upsertRunArgs(UUID.randomUUID(), now, "{}", Utils.checksumFor(ImmutableMap.of()));

    Optional<RunState> runState = Optional.ofNullable(event.getEventType()).map(this::getRunState);
    RunDao runDao = createRunDao();
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
            runState.orElse(null),
            now,
            namespace.getName(),
            newParentJobRow.getName(),
            newParentJobRow.getLocation(),
            newParentJobRow.getJobContextUuid().orElse(null));
    log.info("Created new parent run record {}", newRow);

    runState
        .map(rs -> createRunStateDao().upsert(UUID.randomUUID(), now, uuid, rs))
        .ifPresent(
            runStateRow -> {
              UUID runStateUuid = runStateRow.getUuid();
              if (RunState.valueOf(runStateRow.getState()).isDone()) {
                runDao.updateEndState(uuid, now, runStateUuid);
              } else {
                runDao.updateStartState(uuid, now, runStateUuid);
              }
            });

    return newParentJobRow;
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
      DatasetSymlinkDao datasetSymlinkDao,
      SourceDao sourceDao,
      DatasetDao datasetDao,
      DatasetVersionDao datasetVersionDao,
      DatasetFieldDao datasetFieldDao,
      RunDao runDao,
      ColumnLineageDao columnLineageDao) {
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

    DatasetSymlinkRow symlink =
        datasetSymlinkDao.upsertDatasetSymlinkRow(
            UUID.randomUUID(),
            formatDatasetName(ds.getName()),
            dsNamespace.getUuid(),
            true,
            null,
            now);

    Optional.ofNullable(ds.getFacets())
        .map(facets -> facets.getSymlinks())
        .ifPresent(
            el ->
                el.getIdentifiers().stream()
                    .forEach(
                        id ->
                            datasetSymlinkDao.doUpsertDatasetSymlinkRow(
                                symlink.getUuid(),
                                id.getName(),
                                namespaceDao
                                    .upsertNamespaceRow(
                                        UUID.randomUUID(),
                                        now,
                                        id.getNamespace(),
                                        DEFAULT_NAMESPACE_OWNER)
                                    .getUuid(),
                                false,
                                id.getType(),
                                now)));
    String dslifecycleState =
        Optional.ofNullable(ds.getFacets())
            .map(DatasetFacets::getLifecycleStateChange)
            .map(LifecycleStateChangeFacet::getLifecycleStateChange)
            .orElse("");

    DatasetRow datasetRow =
        datasetDao.upsert(
            symlink.getUuid(),
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
                              symlink.getName(),
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
    List<DatasetFieldRow> datasetFields = new ArrayList<>();
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
        datasetFields.add(datasetFieldRow);
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

    List<ColumnLineageRow> columnLineageRows = Collections.emptyList();
    if (!isInput) {
      columnLineageRows =
          upsertColumnLineage(
              runUuid,
              ds,
              now,
              datasetFields,
              columnLineageDao,
              datasetFieldDao,
              datasetVersionRow);
    }

    return new DatasetRecord(datasetRow, datasetVersionRow, datasetNamespace, columnLineageRows);
  }

  private List<ColumnLineageRow> upsertColumnLineage(
      UUID runUuid,
      Dataset ds,
      Instant now,
      List<DatasetFieldRow> datasetFields,
      ColumnLineageDao columnLineageDao,
      DatasetFieldDao datasetFieldDao,
      DatasetVersionRow datasetVersionRow) {
    // get all the fields related to this particular run
    List<InputFieldData> runFields = datasetFieldDao.findInputFieldsDataAssociatedWithRun(runUuid);

    return Optional.ofNullable(ds.getFacets())
        .map(DatasetFacets::getColumnLineage)
        .map(LineageEvent.ColumnLineageDatasetFacet::getFields)
        .map(LineageEvent.ColumnLineageDatasetFacetFields::getAdditional)
        .stream()
        .flatMap(map -> map.keySet().stream())
        .filter(
            columnName ->
                ds.getFacets().getColumnLineage().getFields().getAdditional().get(columnName)
                    instanceof LineageEvent.ColumnLineageOutputColumn)
        .flatMap(
            columnName -> {
              LineageEvent.ColumnLineageOutputColumn columnLineage =
                  ds.getFacets().getColumnLineage().getFields().getAdditional().get(columnName);
              Optional<DatasetFieldRow> outputField =
                  datasetFields.stream().filter(dfr -> dfr.getName().equals(columnName)).findAny();

              if (outputField.isEmpty()) {
                Logger log = LoggerFactory.getLogger(OpenLineageDao.class);
                log.error(
                    "Cannot produce column lineage for missing output field in output dataset: {}",
                    columnName);
                return Stream.<ColumnLineageRow>empty();
              }

              // get field uuids of input columns related to this run
              List<Pair<UUID, UUID>> inputFields =
                  runFields.stream()
                      .filter(
                          fieldData ->
                              columnLineage.getInputFields().stream()
                                  .filter(
                                      of ->
                                          of.getNamespace().equals(fieldData.getNamespace())
                                              && of.getName().equals(fieldData.getDatasetName())
                                              && of.getField().equals(fieldData.getField()))
                                  .findAny()
                                  .isPresent())
                      .map(
                          fieldData ->
                              Pair.of(
                                  fieldData.getDatasetVersionUuid(),
                                  fieldData.getDatasetFieldUuid()))
                      .collect(Collectors.toList());

              return columnLineageDao
                  .upsertColumnLineageRow(
                      datasetVersionRow.getUuid(),
                      outputField.get().getUuid(),
                      inputFields,
                      columnLineage.getTransformationDescription(),
                      columnLineage.getTransformationType(),
                      now)
                  .stream();
            })
        .collect(Collectors.toList());
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
