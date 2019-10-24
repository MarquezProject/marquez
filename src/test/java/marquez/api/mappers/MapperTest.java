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

// ackage marquez.api.mappers;
//
// mport static java.time.format.DateTimeFormatter.ISO_INSTANT;
// mport static marquez.api.models.ModelGenerator.newNamespaceRequest;
// mport static marquez.common.models.ModelGenerator.newNamespaceName;
// mport static marquez.service.models.ModelGenerator.newNamespace;
// mport static org.assertj.core.api.Assertions.assertThat;
//
// mport marquez.UnitTests;
// mport marquez.api.models.NamespaceRequest;
// mport marquez.api.models.NamespaceResponse;
// mport marquez.common.models.NamespaceName;
// mport marquez.service.models.NamespaceMeta;
// mport marquez.service.models.Namespace;
// mport org.junit.Test;
// mport org.junit.experimental.categories.Category;
//
// Category(UnitTests.class)
// ublic class MapperTest {
// @Test
// public void testToNamespaceMeta() {
//   final NamespaceRequest request = newNamespaceRequest();
//   final NamespaceMeta meta = Mapper.toNamespaceMeta(request);
//   assertThat(meta).isNotNull();
//   assertThat(meta.getOwnerName().getValue()).isEqualTo(request.getOwnerName());
//   assertThat(meta.getDescription()).isNotEmpty();
// }
//
// @Test
// public void testToNamespaceResponse() {
//   final Namespace namespace = newNamespace();
//   final NamespaceResponse response = Mapper.toNamespaceResponse(namespace);
//   assertThat(response).isNotNull();
//   assertThat(response.getName()).isEqualTo(namespace.getName().getValue());
//   assertThat(response.getCreatedAt()).isEqualTo(ISO_INSTANT.format(namespace.getCreatedAt()));
//   assertThat(response.getUpdatedAt()).isEqualTo(ISO_INSTANT.format(namespace.getUpdatedAt()));
//   assertThat(response.getOwnerName()).isEqualTo(namespace.getOwnerName().getValue());
//   assertThat(response.getDescription()).isNotEmpty();
// }
//
