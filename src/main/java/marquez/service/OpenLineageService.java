package marquez.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import marquez.api.model.LineageEvent;
import marquez.db.OpenLineageDao;

@Slf4j
public class OpenLineageService {
  private final OpenLineageDao openLineageDao;
  private final ObjectMapper mapper;

  public OpenLineageService(OpenLineageDao openLineageDao, ObjectMapper lineageObjectMapper) {
    this.openLineageDao = openLineageDao;
    this.mapper = lineageObjectMapper;
  }

  public CompletableFuture<Void> createLineageEvent(LineageEvent event) {
    // Use a separate executor to backfill marquez
    CompletableFuture marquez =
        CompletableFuture.runAsync(() -> openLineageDao.updateMarquezModel(event));

    CompletableFuture openLineage =
        CompletableFuture.runAsync(
            () ->
                openLineageDao.createLineageEvent(
                    event.eventType == null ? "" : event.eventType,
                    event.eventTime.withZoneSameInstant(ZoneId.of("UTC")).toInstant(),
                    event.run.runId,
                    event.job.name,
                    event.job.namespace,
                    openLineageDao.createJsonArray(event.inputs, mapper),
                    openLineageDao.createJsonArray(event.outputs, mapper),
                    event.producer));

    return CompletableFuture.allOf(marquez, openLineage);
  }
}
