package marquez.client.models;

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
@JsonPropertyOrder({"type", "physicalName", "sourceName", "httpMethod", "description", "runId"})
public final class HttpEndpointMeta extends DatasetMeta {
  @Getter private final String httpMethod;

  @Builder
  private HttpEndpointMeta(
      final String physicalName,
      final String sourceName,
      @NonNull final String httpMethod,
      @Nullable final String description,
      @Nullable final String runId) {
    super(physicalName, sourceName, description, runId);
    this.httpMethod = httpMethod;
  }

  @Override
  public String toJson() {
    return Utils.toJson(this);
  }
}
