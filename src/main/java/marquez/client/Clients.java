package marquez.client;

import java.net.URL;
import lombok.NonNull;

/** Allows creating Marquez CLient instances. */
public final class Clients {

  /**
   * Allows updating job status and exploring the marquez model.
   *
   * @param baseUrl The Marquez base url to reaxch the api
   * @return the MarquezClient implementation
   */
  public static MarquezClient newClient(@NonNull final URL baseUrl) {
    return MarquezClient.builder().baseUrl(baseUrl).build();
  }

  /**
   * Allows updating job run status and related lineage Interaction can be synchronous or
   * asynchronous depending on the backend
   *
   * @param backend the underlying protocol to use to transmit information
   * @return the MarquezWriteOnlyClient implementation
   */
  public static MarquezWriteOnlyClient newWriteOnlyClient(@NonNull final Backend backend) {
    return new MarquezWriteOnlyClientImpl(backend);
  }
}
