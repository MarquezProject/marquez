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

package marquez.api.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.api.models.NamespaceResponse;
import marquez.service.models.Namespace;
import org.junit.Test;

public class NamespaceResponseMapperTest {

  UUID nsId = UUID.randomUUID();
  String name = "nsName";
  String ownerName = "someOwner";
  Timestamp createdAt = Timestamp.from(Instant.now());
  String description = "some description";
  Namespace descriptionNamespace = new Namespace(nsId, createdAt, name, ownerName, description);
  Namespace descriptionlessNamespace = new Namespace(nsId, createdAt, name, ownerName, null);

  @Test
  public void testSingleNamespace() {
    NamespaceResponse mappedResponse = NamespaceResponseMapper.map(descriptionNamespace);

    assertThat(mappedResponse.getName()).isEqualTo(descriptionNamespace.getName());
    assertThat(Timestamp.valueOf(mappedResponse.getCreatedAt()))
        .isEqualTo(descriptionNamespace.getCreatedAt());
    assertThat(mappedResponse.getOwner()).isEqualTo(descriptionNamespace.getOwnerName());
    assertThat(mappedResponse.getDescription()).isEqualTo(descriptionNamespace.getDescription());
  }

  @Test
  public void testDescriptionOptional() {
    NamespaceResponse mappedResponse = NamespaceResponseMapper.map(descriptionlessNamespace);

    assertThat(mappedResponse.getDescription()).isNullOrEmpty();
  }

  @Test(expected = NullPointerException.class)
  public void testNullMapListInputs() {
    NamespaceResponseMapper.map((List<Namespace>) null);
  }

  @Test(expected = NullPointerException.class)
  public void testNullMapSingleInput() {
    NamespaceResponseMapper.map((Namespace) null);
  }

  @Test
  public void testList() {
    List<Namespace> namespaces = Arrays.asList(descriptionNamespace, descriptionlessNamespace);
    List<NamespaceResponse> namespaceResponses = NamespaceResponseMapper.map(namespaces);
    assertThat(namespaceResponses).hasSize(2);
    namespaceResponses.stream().forEach(s -> assertThat(s.getName()).isEqualTo(name));
    namespaceResponses.stream()
        .forEach(s -> assertThat(Timestamp.valueOf(s.getCreatedAt())).isEqualTo(createdAt));
  }

  @Test
  public void testEmptyList() {
    List<NamespaceResponse> namespaceResponses =
        NamespaceResponseMapper.map(Collections.emptyList());
    assertThat(namespaceResponses).hasSize(0);
  }
}
