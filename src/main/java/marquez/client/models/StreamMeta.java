package marquez.client.models;

import static marquez.client.models.DatasetType.STREAM;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.client.Utils;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonPropertyOrder({"type", "name", "physicalName", "sourceName", "schemaLocation", "description"})
public final class StreamMeta extends DatasetMeta {
  @Getter private final String schemaLocation;

  @Builder
  private StreamMeta(
      final String physicalName,
      final String sourceName,
      @NonNull final String schemaLocation,
      @Nullable final String description,
      @Nullable final String runId) {
    super(STREAM, physicalName, sourceName, description, runId);
    this.schemaLocation = schemaLocation;
  }

  public String toJson() {
    return Utils.toJson(this);
  }
}
