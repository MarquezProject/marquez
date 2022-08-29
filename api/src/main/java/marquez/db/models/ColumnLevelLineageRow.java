package marquez.db.models;

import lombok.*;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ColumnLevelLineageRow {
    @Getter
    @NonNull
    private final UUID uuid;
    @Getter @NonNull private final UUID datasetVersionUuid;
    @Getter @NonNull private final String outputColumnName;
    @Getter @NonNull private final String inputField;
    @Getter @NonNull private final String transformationDescription;
    @Getter @NonNull private final String transformationType;
    @Getter @NonNull private final Instant createdAt;
    @Getter @NonNull private Instant updatedAt;

}