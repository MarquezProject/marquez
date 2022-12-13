package marquez.cdc;

import io.debezium.config.Configuration;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CaptureChangeEvents {

  public static void startCDC() {
    log.info("Starting cdc app");
    Configuration config =
        Configuration.create()
            .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")
            .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
            .with("offset.storage.file.filename", "/tmp/lineage-offset.dat")
            .with("offset.flush.interval.ms", 1000)
            .with("name", "lineage-events-postgres-connector")
            //.with("database.server.name", localhost-studentDBName)
            .with("database.hostname", "localhost")
            .with("database.port", 54325)
            .with("database.user", "prachi.mishra")
            .with("database.password", "password")
            .with("database.dbname", "marquez")
            .with("table.whitelist", "lineage_events")
            .build();

    try (DebeziumEngine<ChangeEvent<String, String>> engine =
        DebeziumEngine.create(Json.class)
            .using(config.asProperties())
            .notifying(
                record -> {
                  log.info(record.toString());
                })
            .build()) {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.execute(engine);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
