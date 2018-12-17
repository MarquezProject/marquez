package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbTableTest {
  @Test
  public void testNewDbTable() {
    final String dbTable = "marquez";
    assertEquals(dbTable, DbTable.of(dbTable).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testDbTableNull() {
    final String nullDbTable = null;
    DbTable.of(nullDbTable);
  }
}
