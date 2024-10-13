package marquez.searchengine.health;

import com.codahale.metrics.health.HealthCheck;

public class SearchHealthCheck extends HealthCheck {

  @Override
  protected Result check() throws Exception {
    return Result.healthy();
  }
}
