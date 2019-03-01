package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
public class DatasourceResponse {
  @Getter @NonNull private String name;
  @Getter @NonNull private String createdAt;
  @Getter @NonNull private String connectionUrl;
}
