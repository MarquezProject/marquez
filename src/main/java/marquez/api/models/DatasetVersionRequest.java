package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Optional;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = DbTableVersionRequest.class, name = "DB")})
public abstract class DatasetVersionRequest {
  @Getter @NonNull private final DatasetType type;
  private final String description;

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
