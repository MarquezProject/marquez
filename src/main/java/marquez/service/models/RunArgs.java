package marquez.service.models;

import java.sql.Timestamp;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class RunArgs {

  private final String hexDigest;
  private final String json;
  private final Timestamp createdAt;
}
