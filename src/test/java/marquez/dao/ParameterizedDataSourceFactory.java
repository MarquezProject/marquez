package marquez.dao;

import io.dropwizard.db.DataSourceFactory;

public class ParameterizedDataSourceFactory extends DataSourceFactory {

  public ParameterizedDataSourceFactory(
      final String url, final String user, final String driverClass) {
    super();
    this.setUrl(url);
    this.setUser(user);
    this.setDriverClass(driverClass);
  }
}
