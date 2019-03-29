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

import static marquez.common.models.Description.NO_DESCRIPTION;

import marquez.api.models.DatasetRequest;
import marquez.service.models.Dataset;

public final class DatasetMapper {
  private DatasetMapper() {}

  public static Dataset map(@NonNull DatasetRequest request) {
    return Dataset.builder()
        .name(request.getName())
        .description(request.getDescription().orElse(NO_DESCRIPTION))
        .build();
  }
}
