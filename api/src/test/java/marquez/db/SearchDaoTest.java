/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.glassfish.jersey.internal.guava.Lists;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** The test suite for {@link SearchDao}. */
@Tag("IntegrationTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class SearchDaoTest {
  static final int LIMIT = 25;
  static final int NUM_OF_JOBS = 2;
  /**
   * Using {@link DbTestUtils#newJobs(Jdbi, int)} generates 4 input datasets and 3 output datasets
   * for each job inserted into the {@code jobs} table.
   */
  static final int NUM_OF_DATASETS = 12;

  static SearchDao searchDao;

  @BeforeAll
  public static void setUpOnce(final Jdbi jdbi) {
    searchDao = jdbi.onDemand(SearchDao.class);

    DbTestUtils.newDataset(jdbi, "name_ordering_0");
    DbTestUtils.newDataset(jdbi, "name_ordering_1");
    DbTestUtils.newDataset(jdbi, "name_ordering_2");

    DbTestUtils.newDataset(jdbi, "time_ordering_0");
    DbTestUtils.newDataset(jdbi, "time_ordering_1");
    DbTestUtils.newDataset(jdbi, "time_ordering_2");

    DbTestUtils.newJobs(jdbi, NUM_OF_JOBS);
  }

  @Test
  public void testSearch() {
    final String query = "test";
    final List<SearchResult> results = searchDao.search(query, null, SearchSort.NAME, LIMIT);

    // Ensure search results contain N datasets and M jobs.
    assertThat(results).hasSize(NUM_OF_DATASETS + NUM_OF_JOBS);

    // Group search results by result type.
    final List<List<SearchResult>> resultsGroupedByType = groupResultsByType(results);

    // Ensure search results contain exactly N datasets.
    final List<SearchResult> resultsWithOnlyDatasets = resultsGroupedByType.get(0);
    assertThat(resultsWithOnlyDatasets).hasSize(NUM_OF_DATASETS);

    // Ensure search results contain exactly M jobs.
    final List<SearchResult> resultsWithOnlyJobs = resultsGroupedByType.get(1);
    assertThat(resultsWithOnlyJobs).hasSize(NUM_OF_JOBS);
  }

  @Test
  public void testSearch_noResults() {
    final String query = "query_with_no_results";
    final List<SearchResult> results = searchDao.search(query, null, SearchSort.NAME, LIMIT);
    assertThat(results).isEmpty();
  }

  @Test
  public void testSearch_resultsWithOnlyDatasets() {
    final String query = "test";
    final List<SearchResult> resultsWithFilter =
        searchDao.search(query, SearchFilter.DATASET, SearchSort.NAME, LIMIT);

    // Ensure search results with filter contain N datasets.
    assertThat(resultsWithFilter).hasSize(NUM_OF_DATASETS);

    // Group search results with filter by result type.
    final List<List<SearchResult>> resultsWithFilterGroupedByType =
        groupResultsByType(resultsWithFilter);

    // Ensure filtered search results contain exactly N datasets.
    final List<SearchResult> resultsWithFilterOnlyDatasets = resultsWithFilterGroupedByType.get(0);
    assertThat(resultsWithFilterOnlyDatasets).hasSize(NUM_OF_DATASETS);

    // Ensure filtered search results contain no jobs.
    final List<SearchResult> resultsWithFilterOnlyJobs = resultsWithFilterGroupedByType.get(1);
    assertThat(resultsWithFilterOnlyJobs).isEmpty();

    final String queryOnlyDatasets = "test_dataset";
    final List<SearchResult> resultsWithNoFilter =
        searchDao.search(queryOnlyDatasets, null, SearchSort.NAME, LIMIT);

    // Ensure filtered search results contain N datasets.
    assertThat(resultsWithNoFilter).hasSize(NUM_OF_DATASETS);

    // Group filtered search results by result type.
    final List<List<SearchResult>> resultsWithNoFilterGroupedByType =
        groupResultsByType(resultsWithNoFilter);

    // Ensure filtered search results contain exactly N datasets.
    final List<SearchResult> resultsWithNoFilterOnlyDatasets =
        resultsWithNoFilterGroupedByType.get(0);
    assertThat(resultsWithNoFilterOnlyDatasets).hasSize(NUM_OF_DATASETS);

    // Ensure filtered search results contain no jobs.
    final List<SearchResult> resultsWithNoFilterOnlyJobs = resultsWithNoFilterGroupedByType.get(1);
    assertThat(resultsWithNoFilterOnlyJobs).isEmpty();
  }

  @Test
  public void testSearch_resultsWithOnlyJobs() {
    final String query = "test";
    final List<SearchResult> resultsWithFilter =
        searchDao.search(query, SearchFilter.JOB, SearchSort.NAME, LIMIT);

    // Ensure search results with filter contain N jobs.
    assertThat(resultsWithFilter).hasSize(NUM_OF_JOBS);

    // Group filtered search results by result type.
    final List<List<SearchResult>> resultsWithFilterGroupedByType =
        groupResultsByType(resultsWithFilter);

    // Ensure filtered search results contain no datasets.
    final List<SearchResult> resultsWithFilterOnlyDatasets = resultsWithFilterGroupedByType.get(0);
    assertThat(resultsWithFilterOnlyDatasets).isEmpty();

    // Ensure filtered search results contain exactly N jobs.
    final List<SearchResult> resultsWithFilterOnlyJobs = resultsWithFilterGroupedByType.get(1);
    assertThat(resultsWithFilterOnlyJobs).hasSize(NUM_OF_JOBS);

    final String queryOnlyJobs = "test_job";
    final List<SearchResult> resultsWithNoFilter =
        searchDao.search(queryOnlyJobs, null, SearchSort.NAME, LIMIT);

    // Ensure filtered search results contain N jobs.
    assertThat(resultsWithNoFilter).hasSize(NUM_OF_JOBS);

    // Group filtered search results with filter by result type.
    final List<List<SearchResult>> resultsWithNoFilterGroupedByType =
        groupResultsByType(resultsWithNoFilter);

    // Ensure filtered search results contain no datasets.
    final List<SearchResult> resultsWithNoFilterOnlyDatasets =
        resultsWithNoFilterGroupedByType.get(0);
    assertThat(resultsWithNoFilterOnlyDatasets).isEmpty();

    // Ensure filtered search results contain exactly N jobs.
    final List<SearchResult> resultsWithNoFilterOnlyJobs = resultsWithNoFilterGroupedByType.get(1);
    assertThat(resultsWithNoFilterOnlyJobs).hasSize(NUM_OF_JOBS);
  }

  @Test
  public void testSearch_resultsWithNameSorting() {
    final String query = "name_ordering";
    final List<SearchResult> resultsWithSort =
        searchDao.search(query, SearchFilter.DATASET, SearchSort.NAME, LIMIT);

    // Ensure sorted search results contain N datasets.
    assertThat(resultsWithSort).hasSize(3);

    // Ensure search results sorting.
    assertThat(resultsWithSort)
        .extracting("name")
        .contains("name_ordering_0", "name_ordering_1", "name_ordering_2");
  }

  @Test
  public void testSearch_resultsWithTimeSorting() {
    final String query = "time_ordering";
    final List<SearchResult> resultsWithSort =
        searchDao.search(query, SearchFilter.DATASET, SearchSort.UPDATE_AT, LIMIT);

    // Ensure sorted search results contain N datasets.
    assertThat(resultsWithSort).hasSize(3);

    // Ensure search results sorting.
    final Instant time0 = resultsWithSort.get(0).getUpdatedAt();
    final Instant time1 = resultsWithSort.get(1).getUpdatedAt();
    final Instant time2 = resultsWithSort.get(2).getUpdatedAt();
    assertThat(time0).isBefore(time1);
    assertThat(time1).isBefore(time2);
  }

  /** Returns search results grouped by {@link SearchResult.ResultType}. */
  private List<List<SearchResult>> groupResultsByType(@NonNull List<SearchResult> results) {
    final Map<Boolean, List<SearchResult>> resultsByType =
        results.stream()
            .collect(
                Collectors.partitioningBy(
                    result -> result.getType() == SearchResult.ResultType.JOB));
    return Lists.newArrayList(resultsByType.values());
  }
}
