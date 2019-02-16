package marquez.service.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;
import marquez.common.models.DbName;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.db.models.DataSourceRow;
import marquez.service.models.DbTableVersion;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DataSourceRowMapperTest {
  private static final DataSource DATA_SOURCE = DataSource.fromString("postgresql");
  private static final DbName DB_NAME = DbName.fromString("test_db");
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString(
          String.format("jdbc:%s://localhost:5432/%s", DATA_SOURCE.getValue(), DB_NAME.getValue()));
  private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.fromString("test_schema");
  private static final DbTableName DB_TABLE_NAME = DbTableName.fromString("test_table");
  private static final Description DESCRIPTION = Description.fromString("test description");

  @Test
  public void testMap() {
    final Optional<Description> expectedDescription = Optional.of(DESCRIPTION);
    final DbTableVersion dbTableVersion =
        new DbTableVersion(CONNECTION_URL, DB_SCHEMA_NAME, DB_TABLE_NAME, DESCRIPTION);
    final DataSourceRow dataSourceRow = DataSourceRowMapper.map(dbTableVersion);
    assertNotNull(dataSourceRow);
    assertNotNull(dataSourceRow.getUuid());
    assertEquals(DATA_SOURCE.getValue(), dataSourceRow.getName());
    assertEquals(CONNECTION_URL.getRawValue(), dataSourceRow.getConnectionUrl());
    assertEquals(expectedDescription, dbTableVersion.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullDbTableVersion() {
    final DbTableVersion nullDbTableVersion = null;
    DataSourceRowMapper.map(nullDbTableVersion);
  }
}
