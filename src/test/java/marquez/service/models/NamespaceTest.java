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

package marquez.service.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.Test;

public class NamespaceTest {

  private static final UUID NAMESPACE_UUID = UUID.randomUUID();
  private static final String NAMESPACE_NAME = "myNamespace";
  private static final String OWNER_NAME = "myOwner";
  private static final Timestamp CREATED_AT = Timestamp.from(Instant.now());
  private static final String DESCRIPTION = "the first namespace";

  private static final Namespace NAMESPACE =
      new Namespace(NAMESPACE_UUID, CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);

  @Test
  public void testGuidSet() {
    assertThat(NAMESPACE.getGuid()).isEqualTo(NAMESPACE_UUID);
  }

  @Test
  public void testNamespaceEquality() {
    Namespace namespace =
        new Namespace(NAMESPACE_UUID, CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE).isEqualTo(NAMESPACE);
    assertThat(NAMESPACE).isEqualTo(namespace);
    assertThat(namespace).isEqualTo(NAMESPACE);
  }

  @Test
  public void testHashCodeEquality() {
    Namespace namespace =
        new Namespace(NAMESPACE_UUID, CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.hashCode()).isEqualTo(namespace.hashCode());
  }

  @Test
  public void testNamespaceInequalityOnUUID() {
    Namespace namespace =
        new Namespace(UUID.randomUUID(), CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE).isNotEqualTo(namespace);
  }

  @Test
  public void testNamespaceInequalityOnNonIDField() {
    Namespace namespace =
        new Namespace(
            NAMESPACE_UUID, CREATED_AT, "some other namespace name", OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE).isNotEqualTo(namespace);
  }

  @Test
  public void testNamespaceHashcodeInequality() {
    Namespace namespace =
        new Namespace(UUID.randomUUID(), CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.hashCode()).isNotEqualTo(namespace.hashCode());
  }

  @Test
  public void testNamespaceHashcodeInequalityOnNonIdField() {
    Namespace namespace =
        new Namespace(
            NAMESPACE_UUID, CREATED_AT, "some other namespace name", OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.hashCode()).isNotEqualTo(namespace.hashCode());
  }

  @Test
  public void testNamespaceToStringInequality() {
    Namespace namespace =
        new Namespace(UUID.randomUUID(), CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.toString()).isNotEqualTo(namespace.toString());
  }
}
