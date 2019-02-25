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

import java.util.UUID;
import marquez.db.mappers.JobRunStateRowMapper;
import marquez.service.models.JobRunState;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRunStateRowMapper.class)
public interface JobRunStateDao {
  @SqlUpdate(
      "INSERT INTO job_run_states (guid, job_run_guid, state)"
          + "VALUES (:guid, :job_run_guid, :state)")
  void insert(
      @Bind("guid") final UUID guid,
      @Bind("job_run_guid") UUID job_run_guid,
      @Bind("state") final Integer state);

  @SqlQuery("SELECT * FROM job_run_states WHERE guid = :guid")
  JobRunState findById(@Bind("guid") UUID guid);

  @SqlQuery(
      "SELECT * FROM job_run_states WHERE job_run_guid = :jobRunGuid ORDER by transitioned_at DESC")
  JobRunState findByLatestJobRun(@Bind("jobRunGuid") UUID jobRunGuid);
}
