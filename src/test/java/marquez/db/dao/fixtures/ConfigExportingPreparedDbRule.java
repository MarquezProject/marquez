package marquez.db.dao.fixtures;

import com.opentable.db.postgres.embedded.DatabasePreparer;
import com.opentable.db.postgres.junit.PreparedDbRule;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

public class ConfigExportingPreparedDbRule extends PreparedDbRule {

  private final String CONFIG_OUTPUT_PATH;

  public ConfigExportingPreparedDbRule(DatabasePreparer preparer, final String configOutputPath) {
    super(preparer);
    CONFIG_OUTPUT_PATH = configOutputPath;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    preparePostgresConfigFile();
  }

  private void preparePostgresConfigFile() throws SQLException, IOException {
    String url = getTestDatabase().getConnection().getMetaData().getURL();
    int port = Integer.valueOf(url.split("//")[1].split(":")[1].split("/")[0]);

    Map<String, Object> database = new HashMap<>();
    Map<String, Object> dbInfo = new HashMap<>();
    dbInfo.put("driverClass", "org.postgresql.Driver");
    dbInfo.put("user", "postgres");
    dbInfo.put("url", String.format("jdbc:postgresql://localhost:%d/postgres", port));
    database.put("database", dbInfo);

    Yaml yaml = new Yaml();
    FileWriter fw = new FileWriter(CONFIG_OUTPUT_PATH);

    StringWriter writer = new StringWriter();
    yaml.dump(database, writer);
    fw.write(writer.toString());
    fw.close();
  }
}
