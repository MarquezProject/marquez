package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbTest {
  @Test
  public void testNewDb() {
    final String db = "marquez";
    assertEquals(db, Db.of(db).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testDbNull() {
    final String nullDb = null;
    Db.of(nullDb);
  }
}
