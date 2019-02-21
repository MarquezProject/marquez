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
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(JobVersionRowMapper.class)
public interface JobVersionDao {
  @CreateSqlObject
  JobDao createJobDao();

  @SqlQuery("SELECT * FROM job_versions WHERE version = :version")
  JobVersion findByVersion(@Bind("version") UUID version);

  @SqlQuery(
      "SELECT jv.* \n"
          + "FROM job_versions jv\n"
          + "INNER JOIN jobs j "
          + " ON (j.guid = jv.job_guid AND j.name=:job_name)"
          + "INNER JOIN namespaces n "
          + " ON (n.guid = j.namespace_guid AND n.name=:namespace_name)\n"
          + "ORDER BY created_at")
  List<JobVersion> find(@Bind("namespace_name") String namespace, @Bind("job_name") String jobName);

  @SqlQuery(
      "SELECT jv.* \n"
          + "FROM job_versions jv\n"
          + "INNER JOIN jobs j "
          + " ON (j.guid = jv.job_guid AND j.name=:job_name)"
          + "INNER JOIN namespaces n "
          + " ON (n.guid = j.namespace_guid AND n.name=:namespace_name)\n"
          + "ORDER BY created_at DESC \n"
          + "LIMIT 1")
  JobVersion findLatest(@Bind("namespace_name") String namespace, @Bind("job_name") String jobName);

  @SqlUpdate(
      "INSERT INTO job_versions(guid, version, job_guid, uri) VALUES (:guid, :version, :jobGuid, :uri)")
  void insertVersionOnly(@BindBean JobVersion jobVersion);

  @Transaction
  default void insert(JobVersion jobVersion) {
    insertVersionOnly(jobVersion);
    createJobDao().setCurrentVersionGuid(jobVersion.getJobGuid(), jobVersion.getGuid());
  }
}
