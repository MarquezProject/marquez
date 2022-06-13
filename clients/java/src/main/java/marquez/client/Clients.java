/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

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
}
