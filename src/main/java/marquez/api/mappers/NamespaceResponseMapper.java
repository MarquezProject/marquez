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

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.NonNull;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.service.models.Namespace;

public final class NamespaceResponseMapper {
  private NamespaceResponseMapper() {}

  public static NamespaceResponse map(@NonNull Namespace namespace) {
    return new NamespaceResponse(
        namespace.getName(),
        ISO_INSTANT.format(namespace.getCreatedAt()),
        namespace.getOwnerName(),
        namespace.getDescription());
  }

  public static List<NamespaceResponse> map(@NonNull List<Namespace> namespaces) {
    return unmodifiableList(
        namespaces.stream().map(datasource -> map(datasource)).collect(toList()));
  }

  public static NamespacesResponse toNamespacesResponse(@NonNull List<Namespace> namespaces) {
    return new NamespacesResponse(map(namespaces));
  }
}
