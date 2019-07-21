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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import java.util.List;
import lombok.NonNull;
import marquez.api.models.NamespaceResponse;
import marquez.api.models.NamespacesResponse;
import marquez.service.models.Namespace;

public final class NamespaceResponseMapper {
  private NamespaceResponseMapper() {}

  public static NamespaceResponse map(@NonNull final Namespace namespace) {
    return new NamespaceResponse(
        namespace.getName().getValue(),
        ISO_INSTANT.format(namespace.getCreatedAt()),
        ISO_INSTANT.format(namespace.getUpdatedAt()),
        namespace.getOwnerName().getValue(),
        namespace.getDescription().map(description -> description.getValue()).orElse(null));
  }

  public static List<NamespaceResponse> map(@NonNull final List<Namespace> namespaces) {
    return namespaces.stream().map(namespace -> map(namespace)).collect(toImmutableList());
  }

  public static NamespacesResponse toNamespacesResponse(@NonNull final List<Namespace> namespaces) {
    return new NamespacesResponse(map(namespaces));
  }
}
