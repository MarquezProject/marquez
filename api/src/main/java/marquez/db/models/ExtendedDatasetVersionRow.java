/* SPDX-License-Identifier: Apache-2.0 */

package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExtendedDatasetVersionRow extends DatasetVersionRow {
  @Getter private @NonNull String namespaceName;
  @Getter private @NonNull String datasetName;

  public ExtendedDatasetVersionRow(
      @NonNull UUID uuid,
      @NonNull Instant createdAt,
      @NonNull UUID datasetUuid,
      @NonNull UUID version,
      UUID runUuid,
      @NonNull final String namespaceName,
      @NonNull final String datasetName) {
    super(uuid, createdAt, datasetUuid, version, runUuid);
    this.namespaceName = namespaceName;
    this.datasetName = datasetName;
  }
}
