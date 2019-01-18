package marquez.db.models;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

public class NamespaceRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final String NAME = "test_namespace";
  private static final String CURRENT_OWNER_NAME = "test owner";

  @Test
  public void testNewNamespaceOwnershipRow() {
    final String description = "test description";
    final Optional<String> expectedDescription = Optional.of(description);
    final NamespaceRow namespaceRow =
        NamespaceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .name(NAME)
            .description(description)
            .currentOwnerName(CURRENT_OWNER_NAME)
            .build();
    assertEquals(ROW_UUID, namespaceRow.getUuid());
    assertEquals(CREATED_AT, namespaceRow.getCreatedAt());
    assertEquals(UPDATED_AT, namespaceRow.getUpdatedAt());
    assertEquals(NAME, namespaceRow.getName());
    assertEquals(expectedDescription, namespaceRow.getDescription());
    assertEquals(CURRENT_OWNER_NAME, namespaceRow.getCurrentOwnerName());
  }

  @Test
  public void testNewNamespaceOwnershipRow_noDescription() {
    final Optional<String> noDescription = Optional.empty();
    final NamespaceRow namespaceRow =
        NamespaceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .name(NAME)
            .currentOwnerName(CURRENT_OWNER_NAME)
            .build();
    assertEquals(ROW_UUID, namespaceRow.getUuid());
    assertEquals(CREATED_AT, namespaceRow.getCreatedAt());
    assertEquals(UPDATED_AT, namespaceRow.getUpdatedAt());
    assertEquals(NAME, namespaceRow.getName());
    assertEquals(noDescription, namespaceRow.getDescription());
    assertEquals(CURRENT_OWNER_NAME, namespaceRow.getCurrentOwnerName());
  }

  @Test(expected = NullPointerException.class)
  public void testNewNamespaceOwnershipRow_nullUuid() {
    final UUID nullUuid = null;
    NamespaceRow.builder()
        .uuid(nullUuid)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .name(NAME)
        .currentOwnerName(CURRENT_OWNER_NAME)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewNamespaceOwnershipRow_nullCreatedAt() {
    final Instant nullCreatedAt = null;
    NamespaceRow.builder()
        .uuid(ROW_UUID)
        .createdAt(nullCreatedAt)
        .updatedAt(UPDATED_AT)
        .name(NAME)
        .currentOwnerName(CURRENT_OWNER_NAME)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewNamespaceOwnershipRow_nullUpdatedAt() {
    final Instant nullUpdatedAt = null;
    NamespaceRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(nullUpdatedAt)
        .name(NAME)
        .currentOwnerName(CURRENT_OWNER_NAME)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewNamespaceOwnershipRow_nullName() {
    final String nullName = null;
    NamespaceRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .name(nullName)
        .currentOwnerName(CURRENT_OWNER_NAME)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewNamespaceOwnershipRow_nullCurrentOwnerName() {
    final String nullCurrentOwnerName = null;
    NamespaceRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .name(NAME)
        .currentOwnerName(nullCurrentOwnerName)
        .build();
  }
}
