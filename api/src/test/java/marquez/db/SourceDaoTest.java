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

import static marquez.common.models.ModelGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import marquez.DataAccessTests;
import marquez.IntegrationTests;
import marquez.JdbiRuleInit;
import marquez.common.models.SourceName;
import marquez.common.models.SourceType;
import marquez.db.models.SourceRow;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.testing.JdbiRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({DataAccessTests.class, IntegrationTests.class})
public class SourceDaoTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant NOW = Instant.now();
  private static final SourceType TYPE = newDbSourceType();
  private static final SourceName NAME = newSourceName();
  private static final URI CONNECTION_URL = newConnectionUrlFor(TYPE);
  private static final String DESCRIPTION = newDescription();

  @ClassRule public static final JdbiRule dbRule = JdbiRuleInit.init();

  private static SourceDao dao;

  @BeforeClass
  public static void setUpOnce() {
    final Jdbi jdbi = dbRule.getJdbi();
    dao = jdbi.onDemand(SourceDao.class);

    // Add row
    dao.upsert(
        ROW_UUID,
        TYPE.getValue(),
        NOW,
        NOW,
        NAME.getValue(),
        CONNECTION_URL.toASCIIString(),
        DESCRIPTION);
  }

  @Test
  public void testUpsert_nameDoesNotExist() {
    final int rowsBefore = dao.count();

    // Add row
    dao.upsert(
        UUID.randomUUID(),
        TYPE.getValue(),
        NOW,
        NOW,
        newSourceName().getValue(),
        newConnectionUrlFor(TYPE).toASCIIString(),
        newDescription());

    final int rowsAfter = dao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore + 1);
  }

  @Test
  public void testUpsert_nameAlreadyExists() {
    final int rowsBefore = dao.count();

    // Current row
    final SourceRow currentRow = dao.findBy(ROW_UUID).get();

    // Modify row
    final Instant now = Instant.now();
    final SourceRow modifiedRow =
        dao.upsert(
            ROW_UUID,
            TYPE.getValue(),
            now,
            now,
            NAME.getValue(),
            newConnectionUrlFor(TYPE).toASCIIString(),
            newDescription());

    assertThat(modifiedRow.getUuid()).isEqualTo(currentRow.getUuid());
    assertThat(modifiedRow.getType()).isEqualTo(currentRow.getType());
    assertThat(modifiedRow.getCreatedAt()).isEqualTo(currentRow.getCreatedAt());
    assertThat(modifiedRow.getUpdatedAt()).isAfter(currentRow.getUpdatedAt());
    assertThat(modifiedRow.getConnectionUrl()).isNotEqualTo(currentRow.getConnectionUrl());
    assertThat(modifiedRow.getDescription()).isNotEqualTo(currentRow.getDescription());

    final int rowsAfter = dao.count();
    assertThat(rowsAfter).isEqualTo(rowsBefore);
  }

  @Test
  public void testExists() {
    final boolean exists = dao.exists(NAME.getValue());
    assertThat(exists).isTrue();
  }

  @Test
  public void testFindBy_uuid() {
    final Optional<SourceRow> row = dao.findBy(ROW_UUID);
    assertThat(row).isPresent();
  }

  @Test
  public void testFindBy_uuidNotFound() {
    final Optional<SourceRow> row = dao.findBy(UUID.randomUUID());
    assertThat(row).isEmpty();
  }

  @Test
  public void testFindBy_name() {
    final Optional<SourceRow> row = dao.findBy(NAME.getValue());
    assertThat(row).isPresent();
  }

  @Test
  public void testFindBy_nameNotFound() {
    final Optional<SourceRow> row = dao.findBy(newSourceName().getValue());
    assertThat(row).isEmpty();
  }

  @Test
  public void testFindAll() {
    final SourceName name0 = newSourceName();
    final SourceName name1 = newSourceName();
    final SourceName name2 = newSourceName();
    final SourceName name3 = newSourceName();

    // Add rows
    Stream.of(name0, name1, name2, name3)
        .forEach(
            name -> {
              dao.upsert(
                  UUID.randomUUID(),
                  TYPE.getValue(),
                  NOW,
                  NOW,
                  name.getValue(),
                  newConnectionUrlFor(TYPE).toASCIIString(),
                  newDescription());
            });

    final List<SourceRow> rows = dao.findAll(4, 0);
    assertThat(rows).isNotNull().hasSize(4);
  }
}
