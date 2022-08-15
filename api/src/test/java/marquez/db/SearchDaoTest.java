/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.net.URL;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.common.Utils;
import marquez.common.models.JobType;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.JobMeta;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.MapMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PGobject;

/** The test suite for {@link SearchDao}. */
@Tag("DataAccessTests")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class SearchDaoTest {

  static final int LIMIT = 25;
  static final int NUM_OF_JOBS = 2;
  /**
   * Using {@link DbTestUtils#newJobs(Jdbi, int)} generates 4 input datasets and 3 output datasets
   * for each job inserted into the {@code jobs} table.
   */
  static final int NUM_OF_DATASETS = 12;

  public static final String NEW_SYMLINK_TARGET_JOB = "a_new_symlink_target_job";

  static SearchDao searchDao;

  @BeforeAll
  public static void setUpOnce(final Jdbi jdbi) throws SQLException {
    searchDao = jdbi.onDemand(SearchDao.class);

    DbTestUtils.newDataset(jdbi, "name_ordering_0");
    DbTestUtils.newDataset(jdbi, "name_ordering_1");
    DbTestUtils.newDataset(jdbi, "name_ordering_2");

    DbTestUtils.newDataset(jdbi, "time_ordering_0");
    DbTestUtils.newDataset(jdbi, "time_ordering_1");
    DbTestUtils.newDataset(jdbi, "time_ordering_2");

    ImmutableSet<JobRow> jobRows = DbTestUtils.newJobs(jdbi, NUM_OF_JOBS);

    // add a symlinked job - validate that the number of results is the same in the below unit test
    jobRows.stream()
        .findAny()
        .ifPresent(
            j -> {
              try {
                NamespaceRow namespaceRow =
                    jdbi.onDemand(NamespaceDao.class)
                        .findNamespaceByName(j.getNamespaceName())
                        .get();
                JobRow symlinkTargetJob =
                    DbTestUtils.newJobWith(
                        jdbi,
                        namespaceRow.getName(),
                        NEW_SYMLINK_TARGET_JOB,
                        new JobMeta(
                            JobType.valueOf(j.getType()),
                            ImmutableSet.copyOf(j.getInputs()),
                            ImmutableSet.of(),
                            new URL(j.getLocation()),
                            ImmutableMap.of(),
                            j.getDescription().orElse(null),
                            null));
                PGobject inputs = new PGobject();
                inputs.setType("json");
                inputs.setValue(Utils.getMapper().writeValueAsString(j.getInputs()));
                jdbi.onDemand(JobDao.class)
                    .upsertJob(
                        j.getUuid(),
                        JobType.valueOf(j.getType()),
                        j.getCreatedAt(),
                        namespaceRow.getUuid(),
                        namespaceRow.getName(),
                        j.getName(),
                        j.getDescription().orElse(null),
                        j.getJobContextUuid().orElse(null),
                        j.getLocation(),
                        symlinkTargetJob.getUuid(),
                        inputs);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Test
  public void testSearch(Jdbi jdbi) {
    jdbi.withHandle(
            h ->
                h.createQuery("SELECT uuid, name, namespace_name FROM jobs")
                    .map(new MapMapper())
                    .list())
        .forEach(System.out::println);
    jdbi.withHandle(h -> h.createQuery("SELECT * FROM jobs_fqn").map(new MapMapper()).list())
        .forEach(System.out::println);
    jdbi.withHandle(
            h ->
                h.createQuery(
                        "SELECT uuid, name, namespace_name, unnest(aliases) AS alias, symlink_target_uuid FROM jobs_view")
                    .map(new MapMapper())
                    .list())
        .forEach(System.out::println);
    final String query = "test";
    final List<SearchResult> results = searchDao.search(query, null, SearchSort.NAME, LIMIT);

    // Ensure search results contain N datasets and M jobs.
    assertThat(results).hasSize(NUM_OF_DATASETS + NUM_OF_JOBS);

    // Group search results by result type.
    final Map<SearchResult.ResultType, List<SearchResult>> resultsGroupedByType =
        groupResultsByType(results);

    // Ensure search results contain exactly N datasets.
    final List<SearchResult> resultsWithOnlyDatasets =
        resultsGroupedByType.get(SearchResult.ResultType.DATASET);
    assertThat(resultsWithOnlyDatasets).hasSize(NUM_OF_DATASETS);

    // Ensure search results contain exactly M jobs.
    final List<SearchResult> resultsWithOnlyJobs =
        resultsGroupedByType.get(SearchResult.ResultType.JOB);
    assertThat(resultsWithOnlyJobs).hasSize(NUM_OF_JOBS);

    // Even though we searched for "test" and the symlink target doesn't have "test" in its name,
    // it is part of the search results because the original job had "test" in its name.
    assertThat(resultsWithOnlyJobs)
        .filteredOn(j -> j.getName().equals(NEW_SYMLINK_TARGET_JOB))
        .isNotEmpty();
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
    final Map<SearchResult.ResultType, List<SearchResult>> resultsWithFilterGroupedByType =
        groupResultsByType(resultsWithFilter);

    // Ensure filtered search results contain exactly N datasets.
    final List<SearchResult> resultsWithFilterOnlyDatasets =
        resultsWithFilterGroupedByType.get(SearchResult.ResultType.DATASET);
    assertThat(resultsWithFilterOnlyDatasets).hasSize(NUM_OF_DATASETS);

    // Ensure filtered search results contain no jobs.
    assertThat(resultsWithFilterGroupedByType).doesNotContainKey(SearchResult.ResultType.JOB);

    final String queryOnlyDatasets = "test_dataset";
    final List<SearchResult> resultsWithNoFilter =
        searchDao.search(queryOnlyDatasets, null, SearchSort.NAME, LIMIT);

    // Ensure filtered search results contain N datasets.
    assertThat(resultsWithNoFilter).hasSize(NUM_OF_DATASETS);

    // Group filtered search results by result type.
    final Map<SearchResult.ResultType, List<SearchResult>> resultsWithNoFilterGroupedByType =
        groupResultsByType(resultsWithNoFilter);

    // Ensure filtered search results contain exactly N datasets.
    final List<SearchResult> resultsWithNoFilterOnlyDatasets =
        resultsWithNoFilterGroupedByType.get(SearchResult.ResultType.DATASET);
    assertThat(resultsWithNoFilterOnlyDatasets).hasSize(NUM_OF_DATASETS);

    // Ensure filtered search results contain no jobs.
    assertThat(resultsWithNoFilterGroupedByType).doesNotContainKey(SearchResult.ResultType.JOB);
  }

  @Test
  public void testSearch_resultsWithOnlyJobs() {
    final String query = "test";
    final List<SearchResult> resultsWithFilter =
        searchDao.search(query, SearchFilter.JOB, SearchSort.NAME, LIMIT);

    // Ensure search results with filter contain N jobs.
    assertThat(resultsWithFilter).hasSize(NUM_OF_JOBS);

    // Group filtered search results by result type.
    final Map<SearchResult.ResultType, List<SearchResult>> resultsWithFilterGroupedByType =
        groupResultsByType(resultsWithFilter);

    // Ensure filtered search results contain no datasets.
    assertThat(resultsWithFilterGroupedByType).doesNotContainKey(SearchResult.ResultType.DATASET);

    // Ensure filtered search results contain exactly N jobs.
    final List<SearchResult> resultsWithFilterOnlyJobs =
        resultsWithFilterGroupedByType.get(SearchResult.ResultType.JOB);
    assertThat(resultsWithFilterOnlyJobs).hasSize(NUM_OF_JOBS);

    final String queryOnlyJobs = "test_job";
    final List<SearchResult> resultsWithNoFilter =
        searchDao.search(queryOnlyJobs, null, SearchSort.NAME, LIMIT);

    // Ensure filtered search results contain N jobs.
    assertThat(resultsWithNoFilter).hasSize(NUM_OF_JOBS);

    // Group filtered search results with filter by result type.
    final Map<SearchResult.ResultType, List<SearchResult>> resultsWithNoFilterGroupedByType =
        groupResultsByType(resultsWithNoFilter);

    // Ensure filtered search results contain no datasets.
    assertThat(resultsWithNoFilterGroupedByType).doesNotContainKey(SearchResult.ResultType.DATASET);

    // Ensure filtered search results contain exactly N jobs.
    final List<SearchResult> resultsWithNoFilterOnlyJobs =
        resultsWithNoFilterGroupedByType.get(SearchResult.ResultType.JOB);
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
  private Map<SearchResult.ResultType, List<SearchResult>> groupResultsByType(
      @NonNull List<SearchResult> results) {
    return results.stream().collect(Collectors.groupingBy(SearchResult::getType));
  }
}
