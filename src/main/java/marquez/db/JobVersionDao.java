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
import marquez.db.mappers.JobVersionRowMapper;
import marquez.db.models.JobVersionRow;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(JobVersionRowMapper.class)
public interface JobVersionDao {
  enum IoType {
    INPUT,
    OUTPUT;
  }

  @CreateSqlObject
  JobDao createJobDao();

  @Transaction
  default void insertAndUpdate(JobVersionRow row) {
    insert(row);
    row.getInputs()
        .forEach(
            datasetUuid -> {
              insert(row.getUuid(), datasetUuid, IoType.INPUT.toString());
            });
    row.getOutputs()
        .forEach(
            datasetUuid -> {
              insert(row.getUuid(), datasetUuid, IoType.OUTPUT.toString());
            });
    createJobDao().update(row.getJobUuid(), row.getCreatedAt(), row.getVersion());
  }

  @SqlUpdate(
      "INSERT INTO job_versions (uuid, created_at, updated_at, job_uuid, version, location, latest_run_uuid) "
          + "VALUES (:uuid, :createdAt, :updateAt, :jobUuid, :version, :location, :latestRunUuid)")
  void insert(@BindBean JobVersionRow row);

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
      "SELECT j.uuid AS job_uuid, j.namespace_uuid, jv.*, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'INPUT') AS inputs, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'OUTPUT') AS outputs "
          + "FROM job_versions AS jv "
          + "INNER JOIN jobs AS j "
          + "  ON (job_uuid = jv.job_uuid AND j.current_version_uuid = :currentVersionUuid)"
          + "INNER JOIN namespaces AS n "
          + "  ON (j.namespace_uuid = n.uuid) "
          + "WHERE jv.version = :currentVersionUuid ")
  Optional<JobVersionRow> findBy(UUID currentVersionUuid);

  @SqlQuery(
      "SELECT j.uuid AS job_uuid, j.namespace_uuid, jv.*, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'INPUT') AS inputs, "
          + "ARRAY(SELECT dataset_uuid "
          + "      FROM job_versions_io_mapping "
          + "      WHERE job_version_uuid = jv.uuid AND "
          + "            io_type = 'OUTPUT') AS outputs "
          + "FROM job_versions AS jv "
          + "INNER JOIN jobs AS j "
          + "  ON (job_uuid = jv.job_uuid AND j.name = :jobName)"
          + "INNER JOIN namespaces AS n "
          + "  ON (j.namespace_uuid = n.uuid AND n.name = :namespaceName) "
          + "ORDER BY created_at DESC "
          + "LIMIT 1")
  Optional<JobVersionRow> findLatest(String namespaceName, String jobName);

  @SqlQuery(
      "SELECT j.* FROM jobs AS j "
          + "INNER JOIN namespaces AS n "
          + "  ON (n.name = :namespaceName AND j.namespace_uuid = n.uuid) "
          + "ORDER BY j.name "
          + "LIMIT :limit OFFSET :offset")
  List<JobVersionRow> findAll(String namespaceName, String jobName, int limit, int offset);

  @SqlQuery("SELECT COUNT(*) FROM job_versions")
  int count();
}
