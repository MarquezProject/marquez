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
import marquez.db.mappers.JobRowMapper;
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
@RegisterRowMapper(JobRowMapper.class)
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
      "WITH RECURSIVE\n"
          + "    job_io AS (\n"
          + "        SELECT job_uuid,\n"
          + "        ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io_type='INPUT') AS inputs,\n"
          + "        ARRAY_AGG(DISTINCT io.dataset_uuid) FILTER (WHERE io_type='OUTPUT') AS outputs\n"
          + "        FROM jobs j\n"
          + "        INNER JOIN job_versions v on j.current_version_uuid = v.uuid\n"
          + "        LEFT JOIN job_versions_io_mapping io on v.uuid = io.job_version_uuid\n"
          + "        GROUP BY v.job_uuid\n"
          + "    ),\n"
          + "    lineage(job_uuid, inputs, outputs) AS (\n"
          + "        SELECT job_uuid, inputs, outputs, 0 AS depth\n"
          + "        FROM job_io\n"
          + "        WHERE job_uuid IN (<jobIds>)\n"
          + "        UNION\n"
          + "        SELECT io.job_uuid, io.inputs, io.outputs, l.depth + 1\n"
          + "        FROM job_io io,\n"
          + "             lineage l\n"
          + "        WHERE io.job_uuid != l.job_uuid AND\n"
          + "        array_cat(io.inputs, io.outputs) && array_cat(l.inputs, l.outputs)\n"
          + "        AND depth < :depth"
          + "    )\n"
          + "SELECT DISTINCT ON (l2.job_uuid) j.*, inputs AS input_uuids, outputs AS output_uuids, jc.context\n"
          + "FROM lineage l2\n"
          + "INNER JOIN jobs j ON j.uuid=l2.job_uuid\n"
          + "LEFT JOIN job_contexts jc on jc.uuid = j.current_job_context_uuid")
  Set<JobData> getLineage(@BindList Set<UUID> jobIds, int depth);

  @SqlQuery("SELECT uuid from jobs where name = :jobName and namespace_name = :namespace")
  Optional<UUID> getJobUuid(String jobName, String namespace);

  @SqlQuery(
      "SELECT ds.*, dv.fields\n"
          + "FROM datasets ds\n"
          + "LEFT JOIN dataset_versions dv on dv.uuid = ds.current_version_uuid\n"
          + "WHERE ds.uuid IN (<dsUuids>);")
  Set<DatasetData> getDatasetData(@BindList Set<UUID> dsUuids);

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
