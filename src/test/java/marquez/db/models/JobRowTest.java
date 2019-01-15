package marquez.db.models;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

public class JobRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final UUID NAMESPACE_UUID = UUID.randomUUID();
  private static final String NAME = "test job";
  private static final String DESCRIPTION = "test description";
  private static final UUID CURRENT_VERSION_UUID = UUID.randomUUID();

  @Test
  public void testNewJobRow() {
    final Optional<String> nonEmptyDescription = Optional.of(DESCRIPTION);
    final JobRow jobRow =
        JobRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .namespaceUuid(NAMESPACE_UUID)
            .name(NAME)
            .description(DESCRIPTION)
            .currentVersionUuid(CURRENT_VERSION_UUID)
            .build();
    assertEquals(ROW_UUID, jobRow.getUuid());
    assertEquals(CREATED_AT, jobRow.getCreatedAt());
    assertEquals(UPDATED_AT, jobRow.getUpdatedAt());
    assertEquals(NAMESPACE_UUID, jobRow.getNamespaceUuid());
    assertEquals(NAME, jobRow.getName());
    assertEquals(nonEmptyDescription, jobRow.getDescription());
    assertEquals(CURRENT_VERSION_UUID, jobRow.getCurrentVersionUuid());
  }

  @Test
  public void testNewJobRowNoDescription() {
    final Optional<String> noDescription = Optional.empty();
    final JobRow jobRow =
        JobRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .namespaceUuid(NAMESPACE_UUID)
            .name(NAME)
            .currentVersionUuid(CURRENT_VERSION_UUID)
            .build();
    assertEquals(ROW_UUID, jobRow.getUuid());
    assertEquals(CREATED_AT, jobRow.getCreatedAt());
    assertEquals(UPDATED_AT, jobRow.getUpdatedAt());
    assertEquals(NAMESPACE_UUID, jobRow.getNamespaceUuid());
    assertEquals(NAME, jobRow.getName());
    assertEquals(noDescription, jobRow.getDescription());
    assertEquals(CURRENT_VERSION_UUID, jobRow.getCurrentVersionUuid());
  }
}
