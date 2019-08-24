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

import java.util.UUID;
import lombok.NonNull;
import marquez.db.models.NamespaceRow;
import marquez.service.models.Namespace;

public final class NamespaceRowMapper {
  private NamespaceRowMapper() {}

  public static NamespaceRow map(@NonNull Namespace namespace) {
    return NamespaceRow.builder()
        .uuid(UUID.randomUUID())
        .name(namespace.getName())
        .description(namespace.getDescription())
        .currentOwnerName(namespace.getOwner())
        .build();
  }
}
