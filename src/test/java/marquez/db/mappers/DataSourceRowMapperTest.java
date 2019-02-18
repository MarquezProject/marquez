package marquez.db.mappers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.UnitTests;
import marquez.db.models.DataSourceRow;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DataSourceRowMapperTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final String NAME = "postgresql";
  private static final String CONNECTION_URL =
      String.format("jdbc:%s://localhost:5432/test_db", NAME);

  @Test
  public void testMap() throws SQLException {
    final ResultSet results = mock(ResultSet.class);
    when(results.getObject("guid", UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp("created_at")).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getString("name")).thenReturn(NAME);
    when(results.getString("connection_url")).thenReturn(CONNECTION_URL);

    final StatementContext context = mock(StatementContext.class);

    final DataSourceRowMapper dataSourceRowMapper = new DataSourceRowMapper();
    final DataSourceRow dataSourceRow = dataSourceRowMapper.map(results, context);
    assertEquals(ROW_UUID, dataSourceRow.getUuid());
    assertEquals(CREATED_AT, dataSourceRow.getCreatedAt());
    assertEquals(NAME, dataSourceRow.getName());
    assertEquals(CONNECTION_URL, dataSourceRow.getConnectionUrl());
  }

  @Test
  public void testMap_noCreatedAt() throws SQLException {
    final Timestamp noCreatedAt = null;
    final ResultSet results = mock(ResultSet.class);
    when(results.getObject("guid", UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp("created_at")).thenReturn(noCreatedAt);
    when(results.getString("name")).thenReturn(NAME);
    when(results.getString("connection_url")).thenReturn(CONNECTION_URL);

    final StatementContext context = mock(StatementContext.class);

    final DataSourceRowMapper dataSourceRowMapper = new DataSourceRowMapper();
    final DataSourceRow dataSourceRow = dataSourceRowMapper.map(results, context);
    assertEquals(ROW_UUID, dataSourceRow.getUuid());
    assertEquals(noCreatedAt, dataSourceRow.getCreatedAt());
    assertEquals(NAME, dataSourceRow.getName());
    assertEquals(CONNECTION_URL, dataSourceRow.getConnectionUrl());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullResults() throws SQLException {
    final ResultSet nullResults = null;
    final StatementContext context = mock(StatementContext.class);
    final DataSourceRowMapper dataSourceRowMapper = new DataSourceRowMapper();
    dataSourceRowMapper.map(nullResults, context);
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullContext() throws SQLException {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext nullContext = null;
    final DataSourceRowMapper dataSourceRowMapper = new DataSourceRowMapper();
    dataSourceRowMapper.map(results, nullContext);
  }
}
