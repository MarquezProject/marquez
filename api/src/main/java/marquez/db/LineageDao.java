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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import marquez.db.mappers.DatasetDataMapper;
import marquez.db.mappers.JobDataMapper;
import marquez.db.mappers.RunMapper;
import marquez.db.models.DatasetData;
import marquez.db.models.JobData;
import marquez.service.models.Run;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(DatasetDataMapper.class)
@RegisterRowMapper(JobDataMapper.class)
@RegisterRowMapper(RunMapper.class)
public interface LineageDao {

  /**
   * Fetch all of the jobs that consume or produce the datasets that are consumed or produced by the
   * input jobIds. This returns a single layer from the BFS using datasets as edges. Jobs that have
   * no input or output datasets will have no results. Jobs that have no upstream producers or
   * downstream consumers will have the original jobIds returned.
   *
   * @param jobIds
   * @return
   */
  @SqlQuery(
      // dataset_ids: all the input and output datasets of the current version of the specified jobs
      "WITH dataset_ids AS (\n"
          + "    SELECT DISTINCT dataset_uuid\n"
          + "        FROM jobs j\n"
          + "        INNER JOIN job_versions_io_mapping io ON io.job_version_uuid=j.current_version_uuid\n"
          + "        WHERE j.uuid IN (<jobIds>)\n"
          + ")\n"
          // SELECT DISTINCT j.uuid:
          // all the jobs that have ever read from or written to those datasets.
          // note that this includes previous versions of jobs that no longer interact with those
          // datasets
          + "SELECT DISTINCT j.uuid\n"
          + "    FROM job_versions_io_mapping io\n"
          + "    INNER JOIN dataset_ids ON io.dataset_uuid=dataset_ids.dataset_uuid\n"
          + "    INNER JOIN job_versions jv ON jv.uuid=io.job_version_uuid\n"
          + "    INNER JOIN jobs j ON j.uuid=jv.job_uuid")
  Set<UUID> getLineage(@BindList Set<UUID> jobIds);

  @SqlQuery("SELECT uuid from jobs where name = :jobName and namespace_name = :namespace")
  Optional<UUID> getJobUuid(String jobName, String namespace);

  // TODO- the input and output dataset methods below can be combined into a single SQL query
  // that fetches input and output edges for the returned datasets all at once.

  /**
   * Return a list of datasets that are either inputs or outputs of the specified job ids and the
   * <i>output</i> edges of those datasets. Jobs that have no inputs or outputs will return an empty
   * list.
   *
   * @param jobIds
   * @return
   */
  @SqlQuery(
      "WITH dataset_ids AS (\n"
          + "    SELECT DISTINCT dataset_uuid\n"
          + "    FROM jobs j\n"
          + "    INNER JOIN job_versions_io_mapping io ON io.job_version_uuid=j.current_version_uuid\n"
          + "    WHERE j.uuid IN (<jobIds>)\n"
          + ")\n"
          + "SELECT ds.*, COALESCE(job_ids, '{}') AS job_ids, dv.fields\n"
          + "FROM datasets ds\n"
          + "    INNER JOIN dataset_ids ON dataset_ids.dataset_uuid=ds.uuid\n"
          + "    LEFT JOIN (SELECT dataset_uuid, ARRAY_AGG(DISTINCT v.job_uuid) AS job_ids\n"
          + "        FROM job_versions_io_mapping io2\n"
          + "        INNER JOIN job_versions v on io2.job_version_uuid = v.uuid\n"
          + "        WHERE io2.io_type='INPUT'\n"
          + "        GROUP BY dataset_uuid\n"
          + "        ) io ON io.dataset_uuid=ds.uuid\n"
          + "    LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid;")
  List<DatasetData> getInputDatasetsFromJobIds(@BindList Set<UUID> jobIds);

  /**
   * Return a list of datasets that are either inputs or outputs of the specified job ids and the
   * <i>input</i> edges of those datasets. Jobs that have no inputs or outputs will return an empty
   * list.
   *
   * @param jobIds
   * @return
   */
  @SqlQuery(
      "WITH dataset_ids AS (\n"
          + "    SELECT DISTINCT dataset_uuid\n"
          + "    FROM jobs j\n"
          + "    INNER JOIN job_versions_io_mapping io ON io.job_version_uuid=j.current_version_uuid\n"
          + "    WHERE j.uuid IN (<jobIds>)\n"
          + ")\n"
          + "SELECT ds.*, COALESCE(job_ids, '{}') AS job_ids, dv.fields\n"
          + "FROM datasets ds\n"
          + "    INNER JOIN dataset_ids ON dataset_ids.dataset_uuid=ds.uuid\n"
          + "    LEFT JOIN (SELECT dataset_uuid, ARRAY_AGG(DISTINCT v.job_uuid) AS job_ids\n"
          + "        FROM job_versions_io_mapping io2\n"
          + "        INNER JOIN job_versions v on io2.job_version_uuid = v.uuid\n"
          + "        WHERE io2.io_type='OUTPUT'\n"
          + "        GROUP BY dataset_uuid\n"
          + "        ) io ON io.dataset_uuid=ds.uuid\n"
          + "    LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid;")
  List<DatasetData> getOutputDatasetsFromJobIds(@BindList Set<UUID> jobIds);

  @SqlQuery(
      "select j.*, jc.context\n"
          + "from jobs j\n"
          + "left outer join job_contexts jc on jc.uuid = j.current_job_context_uuid\n"
          + "where j.uuid in (<uuid>)")
  List<JobData> getJob(@BindList Collection<UUID> uuid);

  @SqlQuery(
      "select j.uuid from jobs j\n"
          + "inner join job_versions jv on jv.job_uuid = j.uuid\n"
          + "inner join job_versions_io_mapping io on io.job_version_uuid = jv.uuid\n"
          + "inner join datasets ds on ds.uuid = io.dataset_uuid\n"
          + "where ds.name = :datasetName and ds.namespace_name = :namespaceName\n"
          + "order by io_type DESC, jv.created_at DESC\n"
          + "limit 1")
  Optional<UUID> getJobFromInputOrOutput(String datasetName, String namespaceName);

  @SqlQuery(
      "select distinct on(r.job_name, r.namespace_name) r.uuid, r.created_at, r.updated_at, "
          + "r.nominal_start_time, r.nominal_end_time, r.current_run_state, r.started_at, r.ended_at, "
          + "r.namespace_name, r.job_name, r.location, ra.args, ra.args, ctx.context \n"
          + "from runs AS r\n"
          + "inner join jobs j on j.name = r.job_name AND j.namespace_name = r.namespace_name\n"
          + "left outer join run_args AS ra ON ra.uuid = r.run_args_uuid \n"
          + "left outer join job_contexts AS ctx ON r.job_context_uuid = ctx.uuid\n"
          + "where j.uuid in (<jobUuid>)\n"
          + "order by job_name, namespace_name, created_at DESC")
  List<Run> getCurrentRuns(@BindList Collection<UUID> jobUuid);
}
