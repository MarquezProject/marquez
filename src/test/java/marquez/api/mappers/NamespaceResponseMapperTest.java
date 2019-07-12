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

import static marquez.service.models.ServiceModelGenerator.newNamespace;
import static marquez.service.models.ServiceModelGenerator.newNamespaces;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import marquez.UnitTests;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.service.models.Namespace;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceResponseMapperTest {
  @Test
  public void testMap_namespace() {
    final Namespace namespace = newNamespace();
    final NamespaceResponse response = NamespaceResponseMapper.map(namespace);
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo(namespace.getName());
    assertThat(response.getCreatedAt()).isEqualTo(namespace.getCreatedAt().toString());
    assertThat(response.getOwnerName()).isEqualTo(namespace.getOwnerName());
    assertThat(response.getDescription()).isEqualTo(namespace.getDescription());
  }

  @Test
  public void testMap_namespace_noDescription() {
    final Namespace namespace = newNamespace(false);
    final NamespaceResponse response = NamespaceResponseMapper.map(namespace);
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo(namespace.getName());
    assertThat(response.getCreatedAt()).isEqualTo(namespace.getCreatedAt().toString());
    assertThat(response.getOwnerName()).isEqualTo(namespace.getOwnerName());
    assertThat(response.getDescription()).isNull();
  }

  @Test
  public void testMap_throwsException_onNullNamespace() {
    final Namespace nullNamespace = null;
    assertThatNullPointerException().isThrownBy(() -> NamespaceResponseMapper.map(nullNamespace));
  }

  @Test
  public void testMap_namespaces() {
    final List<Namespace> namespaces = newNamespaces(4);
    final List<NamespaceResponse> responses = NamespaceResponseMapper.map(namespaces);
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(4);
  }

  @Test
  public void testMap_emptyNamespaces() {
    final List<Namespace> emptyNamespaces = Collections.emptyList();
    final List<NamespaceResponse> emptyResponses = NamespaceResponseMapper.map(emptyNamespaces);
    assertThat(emptyResponses).isNotNull();
    assertThat(emptyResponses).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullNamespaces() {
    final List<Namespace> nullNamespaces = null;
    assertThatNullPointerException().isThrownBy(() -> NamespaceResponseMapper.map(nullNamespaces));
  }

  @Test
  public void testToNamespacesResponse() {
    final List<Namespace> namespaces = newNamespaces(4);
    final NamespacesResponse response = NamespaceResponseMapper.toNamespacesResponse(namespaces);
    assertThat(response).isNotNull();
    assertThat(response.getNamespaces()).hasSize(4);
  }

  @Test
  public void testToNamespacesResponse_emptyNamespaces() {
    final List<Namespace> emptyNamespaces = Collections.emptyList();
    final NamespacesResponse response =
        NamespaceResponseMapper.toNamespacesResponse(emptyNamespaces);
    assertThat(response).isNotNull();
    assertThat(response.getNamespaces()).isEmpty();
  }

  @Test
  public void testToNamespacesResponse_throwsException_onNullNamespaces() {
    final List<Namespace> nullNamespaces = null;
    assertThatNullPointerException()
        .isThrownBy(() -> NamespaceResponseMapper.toNamespacesResponse(nullNamespaces));
  }
}
