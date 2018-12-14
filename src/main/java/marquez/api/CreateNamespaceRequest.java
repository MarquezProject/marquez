package marquez.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class CreateNamespaceRequest {
  @NotEmpty private String owner;
  @NotEmpty private String description;
}
