package marquez.db.models;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ColumnLevelLineageRow {
  @Getter @NonNull private final UUID uuid;
  @Getter @NonNull private final UUID datasetVersionUuid;
  @Getter @NonNull private final String outputColumnName;
  @Getter @NonNull private final String inputField;
  @Getter @NonNull private final String transformationDescription;
  @Getter @NonNull private final String transformationType;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private Instant updatedAt;
}
