package marquez.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import marquez.service.models.LineageEvent;
import marquez.common.Utils;
import marquez.db.OpenLineageDao;

@Slf4j
public class OpenLineageService {
  private final OpenLineageDao openLineageDao;
  private final ObjectMapper mapper = Utils.newObjectMapper();

  public OpenLineageService(OpenLineageDao openLineageDao) {
    this.openLineageDao = openLineageDao;
  }

  public CompletableFuture<Void> createLineageEvent(LineageEvent event) {
    CompletableFuture marquez =
        CompletableFuture.runAsync(() -> openLineageDao.updateMarquezModel(event));

    CompletableFuture openLineage =
        CompletableFuture.runAsync(
            () ->
                openLineageDao.createLineageEvent(
                    event.getEventType() == null ? "" : event.getEventType(),
                    event.getEventTime().withZoneSameInstant(ZoneId.of("UTC")).toInstant(),
                    event.getRun().getRunId(),
                    event.getJob().getName(),
                    event.getJob().getNamespace(),
                    openLineageDao.createJsonArray(event, mapper),
                    event.getProducer()));

    return CompletableFuture.allOf(marquez, openLineage);
  }
}
