package marquez.service;

import java.util.List;
import marquez.db.OpsDao;
import marquez.db.models.LineageMetric;

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
}
