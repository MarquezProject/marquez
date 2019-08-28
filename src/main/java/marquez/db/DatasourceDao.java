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
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.mappers.DatasourceRowMapper;
import marquez.db.models.DatasourceRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

@RegisterRowMapper(DatasourceRowMapper.class)
public interface DatasourceDao {
  @SqlQuery(
      "INSERT INTO datasources (uuid, urn, name, connection_url) "
          + "VALUES (:uuid, :urn, :name, :connectionUrl) RETURNING * ")
  Optional<DatasourceRow> insert(@BindBean DatasourceRow datasourceRow);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM datasources WHERE urn = :value)")
  boolean exists(@BindBean DatasourceUrn urn);

  @SqlQuery("SELECT * FROM datasources WHERE urn = :value")
  Optional<DatasourceRow> findBy(@BindBean DatasourceUrn urn);

  @SqlQuery("SELECT * FROM datasources WHERE name = :value")
  Optional<DatasourceRow> findBy(@BindBean DatasourceName name);

  @SqlQuery("SELECT * FROM datasources LIMIT :limit OFFSET :offset")
  List<DatasourceRow> findAll(Integer limit, Integer offset);
}
