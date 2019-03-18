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
import marquez.db.mappers.DatasourceRowMapper;
import marquez.db.models.DatasourceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DatasourceRowMapper.class)
public interface DatasourceDao {
  @SqlUpdate(
      "INSERT INTO datasources (guid, urn, name, connection_url) "
          + "VALUES (:uuid, :urn, :name, :connectionUrl)")
  void insert(@BindBean DatasourceRow datasourceRow);

  @SqlQuery("SELECT * FROM datasources WHERE urn = :urn")
  Optional<DatasourceRow> findBy(String urn);

  @SqlQuery("SELECT * FROM datasources LIMIT :limit OFFSET :offset")
  List<DatasourceRow> findAll(Integer limit, Integer offset);
}
