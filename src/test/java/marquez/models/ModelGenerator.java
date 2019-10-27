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

// ackage marquez.service.models;
//
// ublic final class ModelGenerator extends Generator {
// private ModelGenerator() {}
//
//   public static List<Namespace> newNamespaces(final int limit) {
//   return Stream.generate(() -> newNamespace()).limit(limit).collect(toImmutableList());
// }
//
// public static Namespace newNamespace() {
//   return newNamespace(true);
// }
//
// public static Namespace newNamespace(final boolean hasDescription) {
//   return new Namespace(
//       null,
//       newTimestamp(),
//       newNamespaceName().getValue(),
//       newOwnerName().getValue(),
//       hasDescription ? newDescription().getValue() : NO_DESCRIPTION.getValue());
// }
//
