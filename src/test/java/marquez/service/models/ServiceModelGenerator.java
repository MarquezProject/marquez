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

import static java.util.stream.Collectors.toList;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public final class ServiceModelGenerator {
  private ServiceModelGenerator() {}

  public static List<Datasource> newDatasources(int limit) {
    return Stream.generate(() -> newDatasource()).limit(limit).collect(toList());
  }

  public static Datasource newDatasource() {
    return Datasource.builder()
        .name(newDatasourceName())
        .createdAt(newTimestamp())
        .urn(newDatasourceUrn())
        .connectionUrl(newConnectionUrl())
        .build();
  }

  private static Instant newTimestamp() {
    return Instant.now();
  }
}
