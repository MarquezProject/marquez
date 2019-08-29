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

import java.util.List;
import java.util.UUID;
import marquez.db.mappers.JobVersionRowMapper;
import marquez.service.models.JobVersion;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(JobVersionRowMapper.class)
public interface JobVersionDao {
  @CreateSqlObject
  JobDao createJobDao();

  @SqlQuery("SELECT * FROM job_versions WHERE version = :version")
  JobVersion findByVersion(UUID version);

  @SqlQuery(
      "SELECT jv.* \n"
          + "FROM job_versions jv\n"
          + "INNER JOIN jobs j "
          + " ON (j.uuid = jv.job_uuid AND j.name=:jobName)"
          + "INNER JOIN namespaces n "
          + " ON (n.uuid = j.namespace_uuid AND n.name=:namespace)\n"
          + "ORDER BY created_at")
  List<JobVersion> find(String namespace, String jobName);

  @SqlQuery(
      "SELECT jv.* \n"
          + "FROM job_versions jv\n"
          + "INNER JOIN jobs j "
          + " ON (j.uuid = jv.job_uuid AND j.name=:jobName)"
          + "INNER JOIN namespaces n "
          + " ON (n.uuid = j.namespace_uuid AND n.name=:namespace)\n"
          + "ORDER BY created_at DESC \n"
          + "LIMIT 1")
  JobVersion findLatest(String namespace, String jobName);

  @SqlUpdate(
      "INSERT INTO job_versions(uuid, version, job_uuid, uri) VALUES (:uuid, :version, :jobUuid, :uri)")
  void insertVersionOnly(@BindBean JobVersion jobVersion);

  @Transaction
  default void insert(JobVersion jobVersion) {
    insertVersionOnly(jobVersion);
    createJobDao().setCurrentVersionUuid(jobVersion.getJobUuid(), jobVersion.getUuid());
  }
}
