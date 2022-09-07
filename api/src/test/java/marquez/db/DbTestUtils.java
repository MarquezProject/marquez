/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.Generator.newTimestamp;
import static marquez.common.models.CommonModelGenerator.newContext;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newExternalId;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.db.models.DbModelGenerator.newRowUuid;
import static marquez.service.models.ServiceModelGenerator.newDbTableMetaWith;
import static marquez.service.models.ServiceModelGenerator.newJobMetaWith;
import static marquez.service.models.ServiceModelGenerator.newRunMeta;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.JobType;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunState;
import marquez.db.models.DatasetRow;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.JobContextRow;
import marquez.db.models.JobRow;
import marquez.db.models.JobVersionRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.OwnerRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.service.models.Dataset;
import marquez.service.models.DbTableMeta;
import marquez.service.models.JobMeta;
import marquez.service.models.Run;
import marquez.service.models.RunMeta;
import marquez.service.models.ServiceModelGenerator;
import org.jdbi.v3.core.Jdbi;

/** Static utility methods for inserting and interacting with rows in the database. */
final class DbTestUtils {
  private DbTestUtils() {}

  /** Adds a new {@link NamespaceRow} object to the {@code namespaces} table. */
  static NamespaceRow newNamespace(final Jdbi jdbi) {
    final NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    final OwnerRow ownerRow =
        namespaceDao.upsertOwner(newRowUuid(), newTimestamp(), newOwnerName().getValue());
    return namespaceDao.upsertNamespaceRow(
        newRowUuid(),
        newTimestamp(),
        newNamespaceName().getValue(),
        ownerRow.getName(),
        newDescription());
  }

  /** Adds a new {@link DatasetRow} object to the {@code datasets} table. */
  static Dataset newDataset(final Jdbi jdbi) {
    return newDataset(jdbi, newNamespaceName().getValue(), newDatasetName().getValue());
  }

  /**
   * Adds a new {@link DatasetRow} object to the {@code datasets} table with a specified {@code
   * dataset name}.
   */
  static Dataset newDataset(final Jdbi jdbi, final String datasetName) {
    return newDataset(jdbi, newNamespaceName().getValue(), datasetName);
  }

  /**
   * Adds a new {@link DatasetRow} object to the {@code datasets} table with the specified {@code
   * namespace} and {@code dataset name}.
   */
  static Dataset newDataset(final Jdbi jdbi, final String namespace, final String datasetName) {
    final DatasetDao datasetDao = jdbi.onDemand(DatasetDao.class);
    final DbTableMeta dbTableMeta = newDbTableMetaWith(datasetName);
    return datasetDao.upsertDatasetMeta(
        NamespaceName.of(namespace), DatasetName.of(datasetName), dbTableMeta);
  }

  /**
   * Adds new {@link DatasetRow} objects to the {@code datasets} table with a specified {@code
   * limit}.
   */
  static ImmutableSet<Dataset> newDatasets(final Jdbi jdbi, final int limit) {
    return Stream.generate(() -> newDataset(jdbi)).limit(limit).collect(toImmutableSet());
  }

  /** Adds a new {@link JobRow} object to the {@code jobs} table. */
  static JobRow newJob(final Jdbi jdbi) {
    return newJob(jdbi, newNamespaceName().getValue(), newJobName().getValue());
  }

  /**
   * Adds a new {@link JobRow} object to the {@code jobs} table with a specified {@code job name}.
   */
  static JobRow newJob(final Jdbi jdbi, final String jobName) {
    return newJob(jdbi, newNamespaceName().getValue(), jobName);
  }

  /**
   * Adds a new {@link JobRow} object to the {@code jobs} table with the specified {@code namespace}
   * and {@code job name}.
   */
  static JobRow newJob(final Jdbi jdbi, final String namespaceName, final String jobName) {
    return newJobWith(
        jdbi, namespaceName, jobName, newJobMetaWith(NamespaceName.of(namespaceName)));
  }

  /** Adds new {@link JobRow} objects to the {@code jobs} table with a specified {@code limit}. */
  static ImmutableSet<JobRow> newJobs(final Jdbi jdbi, final int limit) {
    return Stream.generate(() -> newJob(jdbi)).limit(limit).collect(toImmutableSet());
  }

  public static JobRow createJobWithoutSymlinkTarget(
      Jdbi jdbi, NamespaceRow namespace, String jobName, String description) {
    return newJobWith(
        jdbi,
        namespace.getName(),
        jobName,
        new JobMeta(
            JobType.BATCH,
            ImmutableSet.of(),
            ImmutableSet.of(),
            null,
            ImmutableMap.of(),
            description,
            null));
  }

  public static JobRow createJobWithSymlinkTarget(
      Jdbi jdbi, NamespaceRow namespace, String jobName, UUID jobSymlinkId, String description) {
    return newJobWith(
        jdbi,
        namespace.getName(),
        jobName,
        jobSymlinkId,
        new JobMeta(
            JobType.BATCH,
            ImmutableSet.of(),
            ImmutableSet.of(),
            null,
            ImmutableMap.of(),
            description,
            null));
  }

  /**
   * Adds a new {@link JobRow} object to the {@code jobs} table with the provided {@link JobMeta}.
   */
  static JobRow newJobWith(
      final Jdbi jdbi, final String namespaceName, final String jobName, final JobMeta jobMeta) {
    return newJobWith(jdbi, namespaceName, jobName, null, jobMeta);
  }

  /**
   * Adds a new {@link JobRow} object to the {@code jobs} table with the provided {@link JobMeta}.
   */
  static JobRow newJobWith(
      final Jdbi jdbi,
      final String namespaceName,
      final String jobName,
      UUID symlinkTargetUuid,
      final JobMeta jobMeta) {
    final DatasetDao datasetDao = jdbi.onDemand(DatasetDao.class);
    final JobDao jobDao = jdbi.onDemand(JobDao.class);

    final NamespaceName namespaceForDatasetAndJob = NamespaceName.of(namespaceName);
    for (final DatasetId jobInputId : jobMeta.getInputs()) {
      datasetDao.upsertDatasetMeta(
          namespaceForDatasetAndJob,
          jobInputId.getName(),
          newDbTableMetaWith(jobInputId.getName()));
    }
    for (final DatasetId jobOutputId : jobMeta.getOutputs()) {
      datasetDao.upsertDatasetMeta(
          namespaceForDatasetAndJob,
          jobOutputId.getName(),
          newDbTableMetaWith(jobOutputId.getName()));
    }

    return jobDao.upsertJobMeta(
        namespaceForDatasetAndJob,
        JobName.of(jobName),
        symlinkTargetUuid,
        jobMeta,
        Utils.getMapper());
  }

  /** Adds a new {@link JobContextRow} object to the {@code job_contexts} table. */
  static JobContextRow newJobContext(final Jdbi jdbi) {
    final JobContextDao jobContextDao = jdbi.onDemand(JobContextDao.class);
    final ImmutableMap<String, String> context = newContext();
    final String contextAsJson = Utils.toJson(newContext());
    final String checksum = Utils.checksumFor(context);
    return jobContextDao.upsert(newRowUuid(), newTimestamp(), contextAsJson, checksum);
  }

  /** Adds a new {@link JobVersionRow} object to the {@code job_versions} table. */
  static ExtendedJobVersionRow newJobVersion(
      final Jdbi jdbi,
      final UUID jobUuid,
      final UUID jobContextUuid,
      final UUID version,
      final String jobName,
      final UUID namespaceUuid,
      final String namespaceName) {
    final JobVersionDao jobVersionDao = jdbi.onDemand(JobVersionDao.class);
    return jobVersionDao.upsertJobVersion(
        newRowUuid(),
        newTimestamp(),
        jobUuid,
        jobContextUuid,
        newLocation().toString(),
        version,
        jobName,
        namespaceUuid,
        namespaceName);
  }

  /** Adds a new {@link RunArgsRow} object to the {@code run_args} table. */
  static RunArgsRow newRunArgs(final Jdbi jdbi) {
    final RunArgsDao runArgsDao = jdbi.onDemand(RunArgsDao.class);
    final ImmutableMap<String, String> runArgs = ServiceModelGenerator.newRunArgs();
    final String runArgsAsJson = Utils.toJson(runArgs);
    final String checksum = Utils.checksumFor(runArgs);
    return runArgsDao.upsertRunArgs(newRowUuid(), newTimestamp(), runArgsAsJson, checksum);
  }

  /** Adds a new {@link RunRow} object to the {@code runs} table. */
  static RunRow newRun(final Jdbi jdbi, JobRow jobRow) {
    final RunDao runDao = jdbi.onDemand(RunDao.class);
    final RunMeta runMeta = newRunMeta();
    return runDao.upsertRunMeta(
        NamespaceName.of(jobRow.getNamespaceName()), jobRow, runMeta, RunState.NEW);
  }

  /** Adds a new {@link RunRow} object to the {@code runs} table. */
  static RunRow newRun(
      final Jdbi jdbi,
      final UUID jobUuid,
      final UUID jobVersionUuid,
      final UUID runArgsUuid,
      final UUID namespaceUuid,
      final String namespaceName,
      final String jobName,
      final String jobLocation,
      final UUID jobContextUuid) {
    final RunDao runDao = jdbi.onDemand(RunDao.class);
    return runDao.upsert(
        newRowUuid(),
        null,
        newExternalId(),
        newTimestamp(),
        jobUuid,
        jobVersionUuid,
        runArgsUuid,
        newTimestamp(),
        newTimestamp(),
        namespaceUuid,
        namespaceName,
        jobName,
        jobLocation,
        jobContextUuid);
  }

  /** Transition a {@link Run} to the provided {@link RunState}. */
  static Run transitionRunTo(final Jdbi jdbi, final UUID runUuid, final RunState runState) {
    final RunDao runDao = jdbi.onDemand(RunDao.class);
    final RunStateDao runStateDao = jdbi.onDemand(RunStateDao.class);
    runStateDao.updateRunStateFor(runUuid, runState, newTimestamp());
    return runDao.findRunByUuid(runUuid).get();
  }

  /**
   * Transition a {@link Run} to the provided {@link RunState} and associates the specified inputs
   * to the run.
   */
  static Run transitionRunWithOutputs(
      final Jdbi jdbi,
      final UUID runUuid,
      final RunState runState,
      final ImmutableSet<DatasetId> runOutputIds) {
    final Run run = transitionRunTo(jdbi, runUuid, runState);
    final RunDao runDao = jdbi.onDemand(RunDao.class);
    runDao.upsertOutputDatasetsFor(runUuid, runOutputIds);
    return run;
  }

  /**
   * Simple utility for querying records from a table and returning the rows as a stream of maps.
   * This is just here for quickly debugging tests and ensuring the contents of the database are
   * what you expect them to be.
   *
   * @param jdbi
   * @param sql
   * @return
   */
  public static Stream<Map<String, Object>> query(Jdbi jdbi, String sql) {
    return jdbi.inTransaction(
        (handle) ->
            handle
                .createQuery(sql)
                .scanResultSet(
                    (rs, ctx) -> {
                      ResultSet resultSet = rs.get();
                      return streamResults(resultSet);
                    }));
  }

  public static Stream<Map<String, Object>> streamResults(ResultSet resultSet) {
    return Stream.generate(
            () -> {
              try {
                if (resultSet.next()) {
                  ResultSetMetaData metaData = resultSet.getMetaData();
                  int keys = metaData.getColumnCount();
                  return IntStream.range(1, keys + 1)
                      .mapToObj(
                          i -> {
                            try {
                              return Map.entry(
                                  metaData.getColumnName(i),
                                  Optional.ofNullable(resultSet.getObject(i)).orElse("NULL"));
                            } catch (SQLException e) {
                              throw new RuntimeException(e);
                            }
                          })
                      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                } else {
                  return null;
                }
              } catch (SQLException e) {
                throw new RuntimeException(e);
              }
            })
        .takeWhile(Predicates.notNull());
  }
}
