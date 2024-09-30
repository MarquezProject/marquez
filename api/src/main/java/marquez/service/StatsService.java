/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import java.util.List;
import marquez.db.StatsDao;
import marquez.db.models.IntervalMetric;
import marquez.db.models.LineageMetric;

public class StatsService {
  private final StatsDao statsDao;

  public StatsService(StatsDao statsDao) {
    this.statsDao = statsDao;
  }

  public List<LineageMetric> getLastDayLineageMetrics() {
    return this.statsDao.getLastDayMetrics();
  }

  public List<LineageMetric> getLastWeekLineageMetrics() {
    return this.statsDao.getLastWeekMetrics();
  }

  public List<IntervalMetric> getLastDayJobs() {
    return this.statsDao.getLastDayJobs();
  }

  public List<IntervalMetric> getLastWeekJobs() {
    return this.statsDao.getLastWeekJobs();
  }

  public List<IntervalMetric> getLastDayDatasets() {
    return this.statsDao.getLastDayDatasets();
  }

  public List<IntervalMetric> getLastWeekDatasets() {
    return this.statsDao.getLastWeekDatasets();
  }
}
