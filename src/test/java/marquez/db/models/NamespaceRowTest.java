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

import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import org.junit.Test;

public class NamespaceRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final NamespaceName NAME = newNamespaceName();
  private static final Description DESCRIPTION = newDescription();
  private static final OwnerName CURRENT_OWNER_NAME = newOwnerName();

  @Test
  public void testNewRow() {
    final NamespaceRow expected =
        NamespaceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME.getValue())
            .description(DESCRIPTION.getValue())
            .currentOwnerName(CURRENT_OWNER_NAME.getValue())
            .build();
    final NamespaceRow actual =
        NamespaceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME.getValue())
            .description(DESCRIPTION.getValue())
            .currentOwnerName(CURRENT_OWNER_NAME.getValue())
            .build();
    assertThat(expected).isEqualTo(actual);
  }

  @Test
  public void testNewRow_noDescription() {
    final NamespaceRow expected =
        NamespaceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME.getValue())
            .currentOwnerName(CURRENT_OWNER_NAME.getValue())
            .build();
    final NamespaceRow actual =
        NamespaceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME.getValue())
            .currentOwnerName(CURRENT_OWNER_NAME.getValue())
            .build();
    assertThat(expected).isEqualTo(actual);
  }
}
