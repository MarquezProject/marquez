/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.ExtendedJobVersionRowMapper;
import marquez.db.models.ExtendedJobVersionRow;
import marquez.db.models.JobVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(ExtendedJobVersionRowMapper.class)
public interface JobVersionDao extends SqlObject {
  enum IoType {
    INPUT,
    OUTPUT;
  }

  @CreateSqlObject
  JobDao createJobDao();

  @Transaction
  default void insert(JobVersionRow row) {
    getHandle()
        .createUpdate(
            "INSERT INTO job_versions (uuid, created_at, updated_at, job_uuid, version, location, latest_run_uuid, job_context_uuid) "
                + "VALUES (:uuid, :createdAt, :updateAt, :jobUuid, :version, :location, :latestRunUuid, :jobContextUuid)")
        .bindBean(row)
        .execute();

    row.getInputUuids()
        .forEach(
            inputUuid -> {
              insert(row.getUuid(), inputUuid, IoType.INPUT.toString());
            });
    row.getOutputUuids()
        .forEach(
            outputUuid -> {
              insert(row.getUuid(), outputUuid, IoType.OUTPUT.toString());
            });

    createJobDao().update(row.getJobUuid(), row.getCreatedAt(), row.getVersion());
  }

  @SqlUpdate(
      "INSERT INTO job_versions_io_mapping (job_version_uuid, dataset_uuid, io_type) "
          + "VALUES (:jobVersionUuid, :datasetUuid, :ioType)")
  void insert(UUID jobVersionUuid, UUID datasetUuid, String ioType);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM job_versions WHERE version = :version)")
  boolean exists(UUID version);

  @SqlUpdate(
      "UPDATE job_versions "
          + "SET updated_at = :updatedAt, "
          + "    latest_run_uuid = :latestRunUuid "
          + "WHERE uuid = :rowUuid")
  void update(UUID rowUuid, Instant updatedAt, UUID latestRunUuid);

  @SqlQuery(
      "SELECT j.uuid AS job_uuid, j.namespace_uuid, jv.*, jc.uuid AS job_context_uuid, jc.context, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'INPUT') AS input_uuids, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'OUTPUT') AS output_uuids "
          + "FROM job_versions AS jv "
          + "INNER JOIN jobs AS j "
          + "  ON (job_uuid = jv.job_uuid AND j.current_version_uuid = :currentVersionUuid)"
          + "INNER JOIN namespaces AS n "
          + "  ON (j.namespace_uuid = n.uuid) "
          + "INNER JOIN job_contexts AS jc "
          + "  ON (job_context_uuid = jc.uuid) "
          + "WHERE jv.version = :currentVersionUuid ")
  Optional<ExtendedJobVersionRow> findBy(UUID currentVersionUuid);

  @SqlQuery(
      "SELECT j.uuid AS job_uuid, j.namespace_uuid, jv.*, jc.uuid AS job_context_uuid, jc.context, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'INPUT') AS input_uuids, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'OUTPUT') AS output_uuids "
          + "FROM job_versions AS jv "
          + "INNER JOIN jobs AS j "
          + "  ON (job_uuid = jv.job_uuid AND j.name = :jobName)"
          + "INNER JOIN namespaces AS n "
          + "  ON (j.namespace_uuid = n.uuid AND n.name = :namespaceName) "
          + "INNER JOIN job_contexts AS jc "
          + "  ON (job_context_uuid = jc.uuid) "
          + "ORDER BY created_at DESC "
          + "LIMIT 1")
  Optional<ExtendedJobVersionRow> findLatest(String namespaceName, String jobName);

  @SqlQuery(
      "SELECT j.uuid AS job_uuid, j.namespace_uuid, jv.*, jc.uuid AS job_context_uuid, jc.context "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'INPUT') AS input_uuids, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'OUTPUT') AS output_uuids "
          + "FROM job_versions AS jv "
          + "INNER JOIN jobs AS j "
          + "  ON (job_uuid = jv.job_uuid AND j.name = :jobName)"
          + "INNER JOIN namespaces AS n "
          + "  ON (j.namespace_uuid = n.uuid AND n.name = :namespaceName) "
          + "INNER JOIN job_contexts AS jc "
          + "  ON (job_context_uuid = jc.uuid) "
          + "ORDER BY created_at DESC "
          + "LIMIT :limit OFFSET :offset")
  List<ExtendedJobVersionRow> findAll(String namespaceName, String jobName, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM job_versions")
  int count();
}
