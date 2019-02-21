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
import marquez.db.mappers.DbTableInfoRowMapper;
import marquez.db.models.DbTableInfoRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(DbTableInfoRowMapper.class)
public interface DbTableInfoDao {
  @SqlUpdate(
      "INSERT INTO db_table_infos (uuid, db_name, db_schema_name) VALUES (:uuid, :db, :dbSchema)")
  void insert(@BindBean DbTableInfoRow dbTableInfoRow);

  @SqlQuery("SELECT * FROM db_table_info WHERE uuid = :uuid")
  Optional<DbTableInfoRow> findBy(@Bind("uuid") UUID uuid);

  @SqlQuery("SELECT * FROM db_table_info LIMIT :limit OFFSET :offset")
  List<DbTableInfoRow> findAll(@Bind("limit") Integer limit, @Bind("offset") Integer offset);
}
