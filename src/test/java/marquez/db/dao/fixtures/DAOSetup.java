package marquez.db.dao.fixtures;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.h2.H2DatabasePlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.testing.junit.DropwizardAppRule;
import marquez.MarquezApplication;
import marquez.MarquezConfiguration;

/**
 * Init code for testing DAOs against h2
 */
public class DAOSetup extends DropwizardAppRule<MarquezConfiguration> {

	/** config file for h2 */
	private static final String config = DAOSetup.class.getResource("/config.test.yaml").getPath();

	private Jdbi jdbi;

	public DAOSetup() {
		super(MarquezApplication.class, config);
	}

	@Override
	protected void before() {
		super.before();
		// init db
		JdbiFactory factory = new JdbiFactory();
		MarquezConfiguration configuration = this.getConfiguration();
		DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();
		ManagedDataSource dataSource = dataSourceFactory.build(this.getEnvironment().metrics(), "h2");
		jdbi = factory
				.build(this.getEnvironment(), dataSourceFactory, dataSource, "h2")
				.installPlugin(new SqlObjectPlugin())
				.installPlugin(new H2DatabasePlugin());

		// setup schema
		FlywayFactory flywayFactory = configuration.getFlywayFactory();
		Flyway flyway = flywayFactory.build(dataSource);
		flyway.migrate();
	}

	public Jdbi getJDBI() {
		return jdbi;
	}

	/**
	 * @see Jdbi#onDemand(Class)
	 */
	public <T> T onDemand(Class<T> classT) {
		return jdbi.onDemand(classT);
	}
}

