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

import static java.util.Objects.requireNonNull;

import marquez.api.models.CreateNamespaceRequest;
import marquez.api.models.NamespaceResponse;
import marquez.service.models.Namespace;

public class NamespaceApiMapper extends Mapper<NamespaceResponse, Namespace> {
  public Namespace map(NamespaceResponse namespace) {
    requireNonNull(namespace, "namespace must not be null");
    return new Namespace(
        null, namespace.getName().toLowerCase(), namespace.getOwner(), namespace.getDescription());
  }

  public Namespace of(String namespaceName, CreateNamespaceRequest request) {
    return map(
        new NamespaceResponse(namespaceName, null, request.getOwner(), request.getDescription()));
  }
}
