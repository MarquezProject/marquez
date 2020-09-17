package marquez.client;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

@Slf4j
class LoggingBackend implements Backend {

  private final Logger logger;

  LoggingBackend() {
    this(log);
  }

  @VisibleForTesting
  LoggingBackend(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void close() throws IOException {
    this.logger.info("closing");
  }

  @Override
  public void put(String path, String json) {
    this.logger.info("PUT " + path + " " + json);
  }

  @Override
  public void post(String path, String json) {
    this.logger.info("POST " + path + (json == null ? "" : " " + json));
  }
}
