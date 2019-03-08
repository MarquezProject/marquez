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
import java.util.Optional;
import java.util.UUID;
import marquez.db.mappers.DataSourceRowMapper;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DataSourceRowMapper.class)
public interface DataSourceDao {
  @SqlUpdate(
      "INSERT INTO datasources (guid, name, connection_url) "
          + "VALUES (:uuid, :name, :connectionUrl)")
  void insert(@BindBean DataSourceRow dataSourceRow);

  @SqlQuery("SELECT * FROM datasources WHERE guid = :uuid")
  Optional<DataSourceRow> findBy(UUID uuid);

  @SqlQuery("SELECT * FROM datasources LIMIT :limit OFFSET :offset")
  List<DataSourceRow> findAll(Integer limit, Integer offset);
}
