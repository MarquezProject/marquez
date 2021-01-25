package marquez.spark.agent.client;

import lombok.Getter;
import lombok.Value;

@Value
public class ResponseMessage<T> {
  @Getter protected int responseCode;
  @Getter protected T body;
  @Getter protected HttpError error;

  public boolean completedSuccessfully() {
    if (responseCode >= 400 && responseCode < 600) { // non-2xx
      return false;
    }
    return true;
  }
}
