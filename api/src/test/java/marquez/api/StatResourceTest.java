/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import marquez.api.models.Period;
import marquez.db.models.IntervalMetric;
import marquez.db.models.LineageMetric;
import marquez.service.ServiceFactory;
import marquez.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatsResourceTest {

  private StatsService statsService;
  private StatsResource statsResource;

  LineageMetric lineageMetric = new LineageMetric(Instant.now(), Instant.now(), 1, 1, 1, 1);
  List<LineageMetric> lineageMetrics = new ArrayList<>();

  IntervalMetric intervalMetric = new IntervalMetric(Instant.now(), Instant.now(), 1);
  List<IntervalMetric> dayMetrics = new ArrayList<>();
  List<IntervalMetric> weekMetrics = new ArrayList<>();

  @BeforeEach
  void setUp() {
    statsService = mock(StatsService.class);
    ServiceFactory serviceFactory = mock(ServiceFactory.class);
    when(serviceFactory.getStatsService()).thenReturn(statsService);
    statsResource = new StatsResource(serviceFactory);
    dayMetrics.add(intervalMetric);
    weekMetrics.add(intervalMetric);
    lineageMetrics.add(lineageMetric);
  }

  @Test
  void testGetStatsDay() {
    when(statsService.getLastDayLineageMetrics()).thenReturn(lineageMetrics);

    Response response = statsResource.getStats(Period.DAY, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(lineageMetrics);
  }

  @Test
  void testGetStatsWeek() {
    when(statsService.getLastWeekLineageMetrics("UTC")).thenReturn(lineageMetrics);

    Response response = statsResource.getStats(Period.WEEK, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(lineageMetrics);
  }

  @Test
  void testGetJobsDay() {
    when(statsService.getLastDayJobs()).thenReturn(dayMetrics);

    Response response = statsResource.getJobs(Period.DAY, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(dayMetrics);
  }

  @Test
  void testGetJobsWeek() {
    when(statsService.getLastWeekJobs("UTC")).thenReturn(weekMetrics);

    Response response = statsResource.getJobs(Period.WEEK, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(weekMetrics);
  }

  @Test
  void testGetJobsInvalidPeriod() {
    Response response = statsResource.getJobs(null, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("Invalid period");
  }

  @Test
  void testGetDatasetsDay() {
    when(statsService.getLastDayDatasets()).thenReturn(dayMetrics);

    Response response = statsResource.getDatasets(Period.DAY, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(dayMetrics);
  }

  @Test
  void testGetDatasetsWeek() {
    when(statsService.getLastWeekDatasets("UTC")).thenReturn(weekMetrics);

    Response response = statsResource.getDatasets(Period.WEEK, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(weekMetrics);
  }

  @Test
  void testGetDatasetsInvalidPeriod() {
    Response response = statsResource.getDatasets(null, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("Invalid period");
  }

  @Test
  void testGetSourcesDay() {
    when(statsService.getLastDaySources()).thenReturn(dayMetrics);

    Response response = statsResource.getSources(Period.DAY, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(dayMetrics);
  }

  @Test
  void testGetSourcesWeek() {
    when(statsService.getLastWeekSources("UTC")).thenReturn(weekMetrics);

    Response response = statsResource.getSources(Period.WEEK, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    assertThat(response.getEntity()).isEqualTo(weekMetrics);
  }

  @Test
  void testGetSourcesInvalidPeriod() {
    Response response = statsResource.getSources(null, "UTC");
    assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    assertThat(response.getEntity()).isEqualTo("Invalid period");
  }
}
