package marquez.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.RunState;
import marquez.db.OpenLineageDao;
import marquez.service.models.LineageEvent;

@Slf4j
public class OpenLineageService {
  private final OpenLineageDao openLineageDao;
  private final RunService runService;
  private final ObjectMapper mapper = Utils.newObjectMapper();

  public OpenLineageService(OpenLineageDao openLineageDao, RunService runService) {
    this.openLineageDao = openLineageDao;
    this.runService = runService;
  }

  public CompletableFuture<Void> createLineageEvent(LineageEvent event) {
    CompletableFuture marquez =
        CompletableFuture.supplyAsync(() -> openLineageDao.updateMarquezModel(event))
            .thenAccept(
                (update) -> {
                  if (event.getEventType() != null
                      && openLineageDao
                          .getRunState(event.getEventType())
                          .equals(RunState.COMPLETED)) {
                    update.getJobInputUpdate().ifPresent(runService::notify);
                    update.getJobOutputUpdate().ifPresent(runService::notify);
                  }
                });

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
