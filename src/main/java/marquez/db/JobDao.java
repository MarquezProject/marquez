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
import marquez.db.mappers.JobRowMapper;
import marquez.service.models.Job;
import marquez.service.models.JobVersion;
import org.jdbi.v3.sqlobject.CreateSqlObject;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

@RegisterRowMapper(JobRowMapper.class)
public interface JobDao {
  @CreateSqlObject
  JobVersionDao createJobVersionDao();

  @SqlUpdate(
      "INSERT INTO jobs (guid, name, namespace_guid, description, input_dataset_urns, output_dataset_urns) "
          + " VALUES (:guid, :name, :namespaceGuid, :description, :inputDatasetUrns, :outputDatasetUrns)")
  public void insert(@BindBean Job job);

  @SqlUpdate("UPDATE jobs SET current_version_guid = :currentVersionGuid WHERE guid = :jobGuid")
  public void setCurrentVersionGuid(UUID jobGuid, UUID currentVersionGuid);

  @Transaction
  default void insertJobAndVersion(final Job job, final JobVersion jobVersion) {
    insert(job);
    createJobVersionDao().insert(jobVersion);
    setCurrentVersionGuid(job.getGuid(), jobVersion.getGuid());
  }

  @SqlQuery(
      "SELECT j.*, jv.uri FROM jobs j INNER JOIN job_versions jv ON (j.guid = :guid AND j.current_version_guid = jv.guid)")
  Job findByID(UUID guid);

  @SqlQuery(
      "SELECT j.*, jv.uri "
          + "FROM jobs j "
          + "INNER JOIN job_versions jv "
          + "    ON (j.current_version_guid = jv.guid) "
          + "INNER JOIN namespaces n "
          + "    ON (j.namespace_guid = n.guid AND n.name = :namespace AND j.name = :name)")
  Job findByName(String namespace, String name);

  @SqlQuery(
      "SELECT j.*, jv.uri "
          + "FROM jobs j "
          + "INNER JOIN job_versions jv "
          + " ON (j.current_version_guid = jv.guid) "
          + "INNER JOIN namespaces n "
          + " ON (j.namespace_guid = n.guid AND n.name = :namespaceName)")
  List<Job> findAllInNamespace(String namespaceName);
}
