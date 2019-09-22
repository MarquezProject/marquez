package marquez.client.models;

import static marquez.client.models.DatasetType.DB_TABLE;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DbTableMeta extends DatasetMeta {
  @Builder
  private DbTableMeta(
      final String physicalName,
      final String sourceName,
      @Nullable final String description,
      @Nullable final String runId) {
    super(DB_TABLE, physicalName, sourceName, description, runId);
  }

  public String toJson() {
    return Utils.toJson(this);
  }
}
