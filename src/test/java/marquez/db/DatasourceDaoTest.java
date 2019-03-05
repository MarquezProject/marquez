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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Generator;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class DatasourceDaoTest {
  private static DatasourceDao datasourceDAO;

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.embeddedPostgres().withPlugin(new SqlObjectPlugin()).migrateWithFlyway();

  @BeforeClass
  public static void setup() {
    final Jdbi jdbi = dbRule.getJdbi();
    datasourceDAO = jdbi.onDemand(DatasourceDao.class);
  }

  @Test
  public void testCreate() {
    final DatasourceRow datasourceRow = Generator.genDatasourceRow();
    datasourceDAO.insert(datasourceRow);
    final Optional<DatasourceRow> returnedRow = datasourceDAO.findBy(datasourceRow.getUuid());
    assertThat(returnedRow).isPresent();

    final DatasourceRow row = returnedRow.get();
    assertThat(row.getConnectionUrl()).isEqualTo(datasourceRow.getConnectionUrl());
    assertThat(row.getName()).isEqualTo(datasourceRow.getName());
    assertThat(row.getCreatedAt()).isPresent();
  }

  @Test
  public void testDatasourceNotPresent() {
    final Optional<DatasourceRow> returnedRow = datasourceDAO.findBy(UUID.randomUUID());
    assertThat(returnedRow).isNotPresent();
  }
}
