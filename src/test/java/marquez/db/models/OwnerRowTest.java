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
