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

import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.StreamVersionRowMapper;
import marquez.db.models.StreamVersionRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(StreamVersionRowMapper.class)
public interface StreamVersionDao {
  @SqlUpdate(
      "INSERT INTO stream_versions (dataset_version_uuid, schema_location) "
          + "VALUES (:uuid, :schemaLocation)")
  void insert(@BindBean StreamVersionRow row);

  @SqlQuery(
      "SELECT * FROM dataset_versions AS dv, stream_versions AS sv "
          + "WHERE dv.version = :version AND sv.dataset_version_uuid = dv.uuid")
  Optional<StreamVersionRow> findBy(UUID version);

  @SqlQuery("SELECT COUNT(*) FROM stream_versions")
  int count();
}
