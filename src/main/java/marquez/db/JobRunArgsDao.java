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

import marquez.db.mappers.JobRunArgsRowMapper;
import marquez.service.models.RunArgs;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(JobRunArgsRowMapper.class)
public interface JobRunArgsDao {
  @SqlUpdate("INSERT INTO job_run_args(hex_digest, args_json) VALUES (:hexDigest, :json)")
  void insert(@BindBean RunArgs runArgs);

  @SqlQuery("SELECT COUNT(*) > 0 FROM job_run_args WHERE hex_digest=:hexDigest")
  boolean digestExists(String hexDigest);

  @SqlQuery("SELECT * FROM job_run_args WHERE hex_digest=:hexDigest LIMIT 1")
  RunArgs findByDigest(String hexDigest);
}
