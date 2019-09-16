package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import marquez.client.utils.JsonUtils;

@JsonPropertyOrder({
  "type",
  "name",
  "physicalName",
  "datasourceName",
  "schemaLocation",
  "description"
})
public final class StreamMeta extends DatasetMeta {
  @Getter @NonNull private final String schemaLocation;

  @Builder
  private StreamMeta(
      String name,
      String physicalName,
      String datasourceName,
      @NonNull String schemaLocation,
      @Nullable String description) {
    super(DatasetType.STREAM, name, physicalName, datasourceName, description);
    this.schemaLocation = schemaLocation;
  }

  public String toJson() {
    return JsonUtils.toJson(this);
  }
}
