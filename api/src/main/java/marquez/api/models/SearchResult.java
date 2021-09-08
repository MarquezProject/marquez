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

package marquez.api.models;

import java.time.Instant;
import lombok.NonNull;
import lombok.Value;
import marquez.common.models.DatasetName;
import marquez.common.models.JobName;
import marquez.common.models.NamespaceName;
import marquez.service.models.NodeId;

/** */
@Value
public class SearchResult {
  /** */
  public enum ResultType {
    DATASET,
    JOB;
  }

  @NonNull ResultType type;
  @NonNull String name;
  @NonNull Instant updatedAt;
  @NonNull NamespaceName namespace;
  @NonNull NodeId nodeId;

  /**
   * @param datasetName
   * @param namespaceName
   * @return
   */
  public static SearchResult newDatasetResult(
      @NonNull final DatasetName datasetName,
      @NonNull final Instant updatedAt,
      @NonNull final NamespaceName namespaceName) {
    return new SearchResult(
        ResultType.DATASET,
        datasetName.getValue(),
        updatedAt,
        namespaceName,
        NodeId.of(namespaceName, datasetName));
  }

  /**
   * @param jobName
   * @param namespaceName
   * @return
   */
  public static SearchResult newJobResult(
      @NonNull final JobName jobName,
      @NonNull final Instant updatedAt,
      @NonNull final NamespaceName namespaceName) {
    return new SearchResult(
        ResultType.JOB,
        jobName.getValue(),
        updatedAt,
        namespaceName,
        NodeId.of(namespaceName, jobName));
  }
}
