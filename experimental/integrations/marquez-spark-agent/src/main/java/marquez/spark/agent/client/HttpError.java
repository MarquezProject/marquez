package marquez.spark.agent.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class HttpError {
  protected Integer code;
  protected String message;
  protected String details;
}
