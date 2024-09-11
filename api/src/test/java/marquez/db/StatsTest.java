/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.api.models.ApiModelGenerator.newRunEvents;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.openlineage.client.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import marquez.db.models.LineageMetric;
import org.jdbi.v3.core.Handle;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTests")
public class StatsTest extends DbTest {

  @Test
  public void testGetStatsForLastDay() {
    // (1) Configure OL.
    final URI olProducer = URI.create("https://test.com/test");
    final OpenLineage ol = new OpenLineage(olProducer);

    // (2) Add namespace and job for OL events.
    final String namespaceName = newNamespaceName().getValue();
    final String jobName = newJobName().getValue();

    // (3) Create some 1 hour old OL events.
    int hourEvents = 4;
    final Set<OpenLineage.RunEvent> hourEventSet =
        newRunEvents(
            ol, Instant.now().minus(1, ChronoUnit.HOURS), namespaceName, jobName, hourEvents);
    DB.insertAll(hourEventSet);

    // (4) Create some 2 day old OL events.
    int dayEvents = 2;
    final Set<OpenLineage.RunEvent> dayEventSet =
        newRunEvents(
            ol, Instant.now().minus(2, ChronoUnit.DAYS), namespaceName, jobName, dayEvents);
    DB.insertAll(dayEventSet);

    // (4) Materialize views to flush out view data.
    try (final Handle handle = DB.open()) {
      DbTestUtils.materializeViews(handle);
    } catch (Exception e) {
      fail("failed to apply dry run", e);
    }

    // Assert the response
    List<LineageMetric> lastDayLineageMetrics = DB.lastDayLineageMetrics();
    List<LineageMetric> lastWeekLineageMetrics = DB.lastWeekLineageMetrics();

    assertThat(lastDayLineageMetrics).isNotEmpty();
    assertThat(lastDayLineageMetrics.get(lastDayLineageMetrics.size() - 1).getComplete())
        .isEqualTo(hourEvents);

    assertThat(lastWeekLineageMetrics).isNotEmpty();
    assertThat(lastWeekLineageMetrics.get(lastWeekLineageMetrics.size() - 2).getComplete())
        .isEqualTo(dayEvents);
  }
}
