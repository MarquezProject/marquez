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

package marquez.service.mappers;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.List;
import lombok.NonNull;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.common.models.OwnerName;
import marquez.db.models.NamespaceRow;
import marquez.service.models.Namespace;

public final class NamespaceMapper {
  private NamespaceMapper() {}

  public static Namespace map(@NonNull final NamespaceRow row) {
    return Namespace.builder()
        .name(NamespaceName.of(row.getName()))
        .createdAt(row.getCreatedAt())
        .updatedAt(row.getUpdatedAt())
        .ownerName(OwnerName.of(row.getCurrentOwnerName()))
        .description(Description.of(row.getDescription()))
        .build();
  }

  public static List<Namespace> map(@NonNull final List<NamespaceRow> rows) {
    return rows.stream().map(row -> map(row)).collect(toImmutableList());
  }
}
