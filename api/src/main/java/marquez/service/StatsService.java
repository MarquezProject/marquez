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

  public List<LineageMetric> getLastWeekLineageMetrics(String timezone) {
    return this.statsDao.getLastWeekMetrics(timezone);
  }

  public List<IntervalMetric> getLastDayJobs() {
    return this.statsDao.getLastDayJobs();
  }

  public List<IntervalMetric> getLastWeekJobs(String timezone) {
    return this.statsDao.getLastWeekJobs(timezone);
  }

  public List<IntervalMetric> getLastDayDatasets() {
    return this.statsDao.getLastDayDatasets();
  }

  public List<IntervalMetric> getLastWeekDatasets(String timezone) {
    return this.statsDao.getLastWeekDatasets(timezone);
  }

  public List<IntervalMetric> getLastDaySources() {
    return this.statsDao.getLastDaySources();
  }

  public List<IntervalMetric> getLastWeekSources(String timezone) {
    return this.statsDao.getLastWeekSources(timezone);
  }
}
