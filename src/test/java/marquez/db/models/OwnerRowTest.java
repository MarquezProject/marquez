package marquez.db.models;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class OwnerRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final String NAME = "test owner";

  @Test
  public void testNewOwnerRow() {
    final OwnerRow ownerRow =
        OwnerRow.builder().uuid(ROW_UUID).createdAt(CREATED_AT).name(NAME).build();
    assertEquals(ROW_UUID, ownerRow.getUuid());
    assertEquals(CREATED_AT, ownerRow.getCreatedAt());
    assertEquals(NAME, ownerRow.getName());
  }

  @Test(expected = NullPointerException.class)
  public void testNewOwnerRow_nullUuid() {
    final UUID nullUuid = null;
    OwnerRow.builder().uuid(nullUuid).createdAt(CREATED_AT).name(NAME).build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewOwnerRow_nullCreatedAt() {
    final Instant nullCreatedAt = null;
    OwnerRow.builder().uuid(ROW_UUID).createdAt(nullCreatedAt).name(NAME).build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewOwnerRow_nullName() {
    final String nullName = null;
    OwnerRow.builder().uuid(ROW_UUID).createdAt(CREATED_AT).name(nullName).build();
  }
}
