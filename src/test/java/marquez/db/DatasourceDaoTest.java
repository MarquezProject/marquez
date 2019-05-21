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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Generator;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

public class DatasourceDaoTest {
  private static DatasourceDao datasourceDAO;

  @ClassRule
  public static final JdbiRule dbRule =
      JdbiRule.embeddedPostgres()
          .withPlugin(new SqlObjectPlugin())
          .withMigration(Migration.before().withDefaultPath());

  @BeforeClass
  public static void setup() {
    final Jdbi jdbi = dbRule.getJdbi();
    datasourceDAO = jdbi.onDemand(DatasourceDao.class);

    datasourceDAO.insert(Generator.genDatasourceRow());
    datasourceDAO.insert(Generator.genDatasourceRow());
    datasourceDAO.insert(Generator.genDatasourceRow());
  }

  @Test
  public void testCreate() {
    final DatasourceRow datasourceRow = Generator.genDatasourceRow();
    datasourceDAO.insert(datasourceRow);

    final Optional<DatasourceRow> returnedRow =
        datasourceDAO.findBy(DatasourceUrn.fromString(datasourceRow.getUrn()));
    assertThat(returnedRow).isPresent();

    final DatasourceRow row = returnedRow.get();
    assertThat(row.getConnectionUrl()).isEqualTo(datasourceRow.getConnectionUrl());
    assertThat(row.getName()).isEqualTo(datasourceRow.getName());
    assertThat(row.getCreatedAt()).isNotNull();
  }

  @Test
  public void testFindByDatasourceName() {
    DatasourceRow row = Generator.genDatasourceRow();
    datasourceDAO.insert(row);
    DatasourceName name = DatasourceName.fromString(row.getName());
    final Optional<DatasourceRow> returnedRow = datasourceDAO.findBy(name);
    assertThat(returnedRow).isPresent();
    assertThat(returnedRow.get().getName()).isEqualTo(row.getName());
  }

  @Test(expected = UnableToExecuteStatementException.class)
  public void testInsertDuplicateRow_ThrowsDaoException() {
    final DatasourceRow datasourceRow = Generator.genDatasourceRow();
    datasourceDAO.insert(datasourceRow);

    final Optional<DatasourceRow> returnedRow =
        datasourceDAO.findBy(DatasourceUrn.fromString(datasourceRow.getUrn()));
    assertThat(returnedRow).isPresent();

    datasourceDAO.insert(datasourceRow);
    assertThat(datasourceDAO.findBy(DatasourceUrn.fromString(datasourceRow.getUrn())))
        .isEqualTo(returnedRow);
  }

  @Test(expected = UnableToExecuteStatementException.class)
  public void testUniquenessConstraintOnName() {
    final ConnectionUrl connectionUrl =
        ConnectionUrl.fromString("jdbc:postgresql://localhost:5431/novelists_");
    final ConnectionUrl connectionUrl2 =
        ConnectionUrl.fromString("jdbc:postgresql://localhost:9999/novelists_");

    final DatasourceName datasourceName = DatasourceName.fromString("Datasource");
    final String datasourceUrn = DatasourceUrn.from(connectionUrl, datasourceName).getValue();

    final DatasourceRow datasourceRow =
        DatasourceRow.builder()
            .uuid(UUID.randomUUID())
            .urn(datasourceUrn)
            .name(datasourceName.getValue())
            .connectionUrl(connectionUrl.getRawValue())
            .createdAt(Instant.now())
            .build();

    final DatasourceRow sameNameRow =
        DatasourceRow.builder()
            .uuid(UUID.randomUUID())
            .urn(DatasourceUrn.from(connectionUrl2, datasourceName).getValue())
            .name(datasourceName.getValue())
            .connectionUrl(connectionUrl.getRawValue())
            .createdAt(Instant.now())
            .build();

    Optional<DatasourceRow> insertedRow = datasourceDAO.insert(datasourceRow);
    assertThat(insertedRow).isPresent();
    assertThat(insertedRow.get().getConnectionUrl()).isEqualTo(connectionUrl.getRawValue());

    datasourceDAO.insert(sameNameRow);
  }

  @Test
  public void testDatasourceNotPresent() {
    DatasourceUrn datasourceUrn = DatasourceUrn.fromString(Generator.genDatasourceRow().getUrn());
    final Optional<DatasourceRow> returnedRow = datasourceDAO.findBy(datasourceUrn);
    assertThat(returnedRow).isNotPresent();
  }

  @Test
  public void testLimit() {
    final List<DatasourceRow> returnedRows = datasourceDAO.findAll(2, 0);
    assertThat(returnedRows.size()).isEqualTo(2);
  }

  @Test
  public void testOffset() {
    final List<DatasourceRow> returnedRows = datasourceDAO.findAll(100, 0);
    final int returnedRowCount = returnedRows.size();

    final int offset = 1;

    final List<DatasourceRow> returnedRowsWithOffset = datasourceDAO.findAll(100, offset);
    final int returnedRowsWithOffsetSize = returnedRowsWithOffset.size();
    assertThat(returnedRowCount - returnedRowsWithOffsetSize).isEqualTo(offset);
  }
}
