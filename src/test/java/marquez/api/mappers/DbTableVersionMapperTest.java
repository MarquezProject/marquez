package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import marquez.api.models.DatasetType;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;
import org.junit.Test;

public class DbTableVersionMapperTest {
  private static final DatasetType DB_TYPE = DatasetType.DB;
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.of("jdbc:postgresql://localhost:5431/novelists");
  private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.of("marquez");
  private static final DbTableName DB_TABLE_NAME = DbTableName.of("quotes");
  private static final Description DESCRIPTION =
      Description.of("It's enough for me to be sure that you and I exist as this moment.");

  @Test
  public void testMapDbTableVersionRequest() {
    final Optional<Description> nonEmptyDescription = Optional.of(DESCRIPTION);
    final DbTableVersionRequest dbTableVersionRequest =
        new DbTableVersionRequest(
            DB_TYPE,
            CONNECTION_URL.getRawValue(),
            DB_SCHEMA_NAME.getValue(),
            DB_TABLE_NAME.getValue(),
            DESCRIPTION.getValue());
    final DbTableVersion dbTableVersion = DbTableVersionMapper.map(dbTableVersionRequest);
    assertNotNull(dbTableVersion);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchemaName());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTableName());
    assertEquals(nonEmptyDescription, dbTableVersion.getDescription());
  }

  @Test
  public void testMapDbTableVersionRequestNoDescription() {
    final Optional<Description> noDescription = Optional.of(NO_DESCRIPTION);
    final DbTableVersionRequest dbTableVersionRequest =
        new DbTableVersionRequest(
            DB_TYPE,
            CONNECTION_URL.getRawValue(),
            DB_SCHEMA_NAME.getValue(),
            DB_TABLE_NAME.getValue(),
            NO_DESCRIPTION.getValue());
    final DbTableVersion dbTableVersion = DbTableVersionMapper.map(dbTableVersionRequest);
    assertNotNull(dbTableVersion);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchemaName());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTableName());
    assertEquals(noDescription, dbTableVersion.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDbTableVersionRequest() {
    final DbTableVersionRequest nullDbTableVersionRequest = null;
    DbTableVersionMapper.map(nullDbTableVersionRequest);
  }
}
