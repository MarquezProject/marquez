/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
  private static final UUID CURRENT_VERSION_UUID = UUID.randomUUID();

  @Test
  public void testNewJobRow() {
    final String description = "test description";
    final Optional<String> expectedDescription = Optional.of(description);
    final JobRow jobRow =
        JobRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .namespaceUuid(NAMESPACE_UUID)
            .name(NAME)
            .description(description)
            .currentVersionUuid(CURRENT_VERSION_UUID)
            .build();
    assertEquals(ROW_UUID, jobRow.getUuid());
    assertEquals(CREATED_AT, jobRow.getCreatedAt());
    assertEquals(UPDATED_AT, jobRow.getUpdatedAt());
    assertEquals(NAMESPACE_UUID, jobRow.getNamespaceUuid());
    assertEquals(NAME, jobRow.getName());
    assertEquals(expectedDescription, jobRow.getDescription());
    assertEquals(CURRENT_VERSION_UUID, jobRow.getCurrentVersionUuid());
  }

  @Test
  public void testNewJobRow_noDescription() {
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

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullUuid() {
    final UUID nullUuid = null;
    JobRow.builder()
        .uuid(nullUuid)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(NAMESPACE_UUID)
        .name(NAME)
        .currentVersionUuid(CURRENT_VERSION_UUID)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullCreatedAt() {
    final Instant nullCreatedAt = null;
    JobRow.builder()
        .uuid(ROW_UUID)
        .createdAt(nullCreatedAt)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(NAMESPACE_UUID)
        .name(NAME)
        .currentVersionUuid(CURRENT_VERSION_UUID)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullUpdatedAt() {
    final Instant nullUpdatedAt = null;
    JobRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(nullUpdatedAt)
        .namespaceUuid(NAMESPACE_UUID)
        .name(NAME)
        .currentVersionUuid(CURRENT_VERSION_UUID)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullNamespaceUuid() {
    final UUID nullNamespaceUuid = null;
    JobRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(nullNamespaceUuid)
        .name(NAME)
        .currentVersionUuid(CURRENT_VERSION_UUID)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullName() {
    final String nullName = null;
    JobRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(NAMESPACE_UUID)
        .name(nullName)
        .currentVersionUuid(CURRENT_VERSION_UUID)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullCurrentVersionUuid() {
    final UUID nullCurrentVersionUuid = null;
    JobRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .namespaceUuid(NAMESPACE_UUID)
        .name(NAME)
        .currentVersionUuid(nullCurrentVersionUuid)
        .build();
  }
}
