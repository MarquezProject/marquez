package marquez.service;

import java.util.List;
import marquez.db.OpsDao;
import marquez.db.models.LineageMetric;
import marquez.service.models.LineageEvent;

public class OpsService {
  private final OpsDao opsDao;

  public OpsService(OpsDao opsDao) {
    this.opsDao = opsDao;
  }

  public List<LineageMetric> getLastDayLineageMetrics() {
    return this.opsDao.getLastDayMetrics();
  }

  public List<LineageMetric> getLastWeekLineageMetrics() {
    return this.opsDao.getLastWeekMetrics();
  }

  public void createLineageMetric(LineageEvent lineageEvent) {
    this.opsDao.createLineageMetric(
        lineageEvent.getEventTime().toInstant(), lineageEvent.getEventType());
  }
}
