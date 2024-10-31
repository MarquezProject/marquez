package marquez.db.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import lombok.Value;

@Value
public class AlertRow {
  @NonNull UUID uuid;
  @NonNull Instant createdAt;
  @NonNull String entityType;
  @NonNull String name;
  @NonNull String type;
  JsonNode config;
}
