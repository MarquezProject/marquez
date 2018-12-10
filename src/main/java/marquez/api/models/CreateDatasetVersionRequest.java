package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NonNull;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CreateDbDatasetVersionRequest.class, name = "DB"),
})
public abstract class CreateDatasetVersionRequest {
  @NonNull private final DatasetType type;
}
