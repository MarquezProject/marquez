package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbSchemaTest {
  @Test
  public void testNewDbSchema() {
    final String dbSchema = "marquez";
    assertEquals(dbSchema, DbSchema.of(dbSchema).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testDbSchemaNull() {
    final String nullDbSchema = null;
    DbSchema.of(nullDbSchema);
  }
}
