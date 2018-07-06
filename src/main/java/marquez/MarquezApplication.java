package marquez;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class MarquezApplication extends Application<MarquezConfiguration> {

  @Override
  public void run(final MarquezConfiguration configuration,
                  final Environment environment) {
    // TODO:
  }

  public static void main(final String[] args) throws Exception {
    final MarquezApplication application = new MarquezApplication();
    application.run(args);
  }
}
