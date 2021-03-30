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
  @SqlQuery(
      "select distinct io_in_2.job_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_inputs io_in_2 on io_in.dataset_uuid = io_in_2.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION \n"
          + "select distinct io_out_2.job_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_outputs io_out_2 on io_out_2.dataset_uuid = io_out.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION \n"
          + "select distinct io_out.job_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.job_version_uuid = j.current_version_uuid \n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.dataset_uuid = io_in.dataset_uuid \n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION \n"
          + "select distinct io_in.job_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.dataset_uuid = io_out.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)")
  Set<UUID> getLineage(@BindList Set<UUID> jobIds);

  @SqlQuery("SELECT uuid from jobs where name = :jobName and namespace_name = :namespace")
  Optional<UUID> getJobUuid(String jobName, String namespace);

  @SqlQuery(
      "select ds.*, ARRAY(select m.uuid) as job_ids, dv.fields\n"
          + "from datasets ds\n"
          + "inner join (\n"
          + "select distinct io_in_2.job_uuid as uuid, io_in_2.dataset_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_inputs io_in_2 on io_in.dataset_uuid = io_in_2.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION\n"
          + "select distinct j.uuid as uuid, io_in.dataset_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.job_version_uuid = j.current_version_uuid\n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION\n"
          + "select distinct io_in.job_uuid as uuid, io_in.dataset_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.dataset_uuid = io_out.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)"
          + ") m on m.dataset_uuid = ds.uuid\n"
          + "left outer join dataset_versions dv on dv.uuid = ds.current_version_uuid\n")
  List<DatasetData> getInputDatasetsFromJobIds(@BindList Set<UUID> jobIds);

  @SqlQuery(
      "select ds.*, ARRAY(select m.uuid) as job_ids, dv.fields\n"
          + "from datasets ds\n"
          + "inner join (\n"
          + "select distinct io_out_2.job_uuid as uuid, io_out_2.dataset_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_outputs io_out_2 on io_out_2.dataset_uuid = io_out.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION\n"
          + "select distinct io_out.job_uuid as uuid, io_out.dataset_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.job_version_uuid = j.current_version_uuid\n"
          + "where j.uuid in (<jobIds>)\n"
          + "UNION\n"
          + "select distinct io_out.job_uuid as uuid, io_out.dataset_uuid\n"
          + "from jobs j\n"
          + "inner join job_versions_io_mapping_inputs io_in on io_in.job_version_uuid = j.current_version_uuid\n"
          + "inner join job_versions_io_mapping_outputs io_out on io_out.dataset_uuid = io_in.dataset_uuid\n"
          + "where j.uuid in (<jobIds>)"
          + ") m on m.dataset_uuid = ds.uuid\n"
          + "left outer join dataset_versions dv on dv.uuid = ds.current_version_uuid\n")
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
