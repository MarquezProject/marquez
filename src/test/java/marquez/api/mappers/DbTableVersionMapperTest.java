package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import marquez.api.models.DatasetType;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchema;
import marquez.common.models.DbTable;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;
import org.junit.Test;

public class DbTableVersionMapperTest {
  private static final DatasetType DB_TYPE = DatasetType.DB;
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.of("jdbc:postgresql://localhost:5432/marquez");
  private static final DbSchema DB_SCHEMA = DbSchema.of("novelists");
  private static final DbTable DB_TABLE = DbTable.of("marquez");
  private static final Description DESCRIPTION =
      Description.of("It's enough for me to be sure that you and I exist as this moment");

  @Test
  public void testMapDbTableVersionRequest() {
    final Optional<Description> nonEmptyDescription = Optional.of(DESCRIPTION);
    final DbTableVersionRequest dbTableVersionRequest =
        new DbTableVersionRequest(
            DB_TYPE,
            CONNECTION_URL.getRawValue(),
            DB_SCHEMA.getValue(),
            DB_TABLE.getValue(),
            DESCRIPTION.getValue());
    final DbTableVersion dbTableVersion = DbTableVersionMapper.map(dbTableVersionRequest);
    assertNotNull(dbTableVersion);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA, dbTableVersion.getDbSchema());
    assertEquals(DB_TABLE, dbTableVersion.getDbTable());
    assertEquals(nonEmptyDescription, dbTableVersion.getDescription());
  }

  @Test
  public void testMapDbTableVersionRequestNoDescription() {
    final Optional<Description> noDescription = Optional.of(NO_DESCRIPTION);
    final DbTableVersionRequest dbTableVersionRequest =
        new DbTableVersionRequest(
            DB_TYPE,
            CONNECTION_URL.getRawValue(),
            DB_SCHEMA.getValue(),
            DB_TABLE.getValue(),
            NO_DESCRIPTION.getValue());
    final DbTableVersion dbTableVersion = DbTableVersionMapper.map(dbTableVersionRequest);
    assertNotNull(dbTableVersion);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA, dbTableVersion.getDbSchema());
    assertEquals(DB_TABLE, dbTableVersion.getDbTable());
    assertEquals(noDescription, dbTableVersion.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDbTableVersionRequest() {
    final DbTableVersionRequest nullDbTableVersionRequest = null;
    DbTableVersionMapper.map(nullDbTableVersionRequest);
  }
}
