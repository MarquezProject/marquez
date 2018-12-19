package marquez.service.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DataSource;
import marquez.common.models.DbSchema;
import marquez.common.models.DbTable;
import marquez.common.models.Description;
import marquez.db.models.DataSourceRow;
import marquez.service.models.DbTableVersion;
import org.junit.Test;

public class DataSourceRowMapperTest {
  private static final DataSource DATA_SOURCE = DataSource.of("postgresql");
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.of(String.format("jdbc:%s://localhost:5432/novelists", DATA_SOURCE.getValue()));
  private static final DbSchema DB_SCHEMA = DbSchema.of("marquez");
  private static final DbTable DB_TABLE = DbTable.of("quotes");
  private static final Description DESCRIPTION =
      Description.of("It's enough for me to be sure that you and I exist as this moment.");

  @Test
  public void testMapDbTableVersion() {
    final Optional<Description> nonEmptyDescription = Optional.of(DESCRIPTION);
    final DbTableVersion dbTableVersion =
        new DbTableVersion(CONNECTION_URL, DB_SCHEMA, DB_TABLE, DESCRIPTION);
    final DataSourceRow dataSourceRow = DataSourceRowMapper.map(dbTableVersion);
    assertNotNull(dataSourceRow);
    assertNotNull(dataSourceRow.getUuid());
    assertEquals(DATA_SOURCE.getValue(), dataSourceRow.getDataSource());
    assertEquals(CONNECTION_URL.getRawValue(), dataSourceRow.getConnectionUrl());
    assertEquals(nonEmptyDescription, dbTableVersion.getDescription());
  }

  @Test
  public void testMapDbTableVersionNoDescription() {
    final Optional<Description> noDescription = Optional.of(NO_DESCRIPTION);
    final DbTableVersion dbTableVersion =
        new DbTableVersion(CONNECTION_URL, DB_SCHEMA, DB_TABLE, NO_DESCRIPTION);
    final DataSourceRow dataSourceRow = DataSourceRowMapper.map(dbTableVersion);
    assertNotNull(dataSourceRow);
    assertNotNull(dataSourceRow.getUuid());
    assertEquals(DATA_SOURCE.getValue(), dataSourceRow.getDataSource());
    assertEquals(CONNECTION_URL.getRawValue(), dataSourceRow.getConnectionUrl());
    assertEquals(noDescription, dbTableVersion.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDbTableVersion() {
    final DbTableVersion nullDbTableVersion = null;
    DataSourceRowMapper.map(nullDbTableVersion);
  }
}
