package marquez.client;

import java.io.IOException;

/** A backend that does not do anything. */
class NullBackend implements Backend {

  @Override
  public void put(String path, String json) {}

  @Override
  public void post(String path, String json) {}

  @Override
  public void close() throws IOException {}
}
