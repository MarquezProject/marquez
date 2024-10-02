/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import java.time.Instant;
import java.util.List;
import marquez.db.StatsDao;
import marquez.db.models.LineageMetric;

public class StatsService {
  private final StatsDao statsDao;

  public StatsService(StatsDao statsDao) {
    this.statsDao = statsDao;
  }

  public void createCurrentHourLineageMetric(Instant metricTime, String state) {
    this.statsDao.createCurrentHourLineageMetric(metricTime, state);
  }

  public void createCurrentDayLineageMetric(Instant metricTime, String state) {
    this.statsDao.createCurrentDayLineageMetric(metricTime, state);
  }

  public List<LineageMetric> getLastDayLineageMetrics() {
    return this.statsDao.getLastDayMetrics();
  }

  public List<LineageMetric> getLastWeekLineageMetrics(String timezone) {
    return this.statsDao.getLastWeekMetrics(timezone);
  }
}
