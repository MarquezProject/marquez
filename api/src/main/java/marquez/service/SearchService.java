package marquez.service;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.models.SearchFilter;
import marquez.api.models.SearchOrder;
import marquez.api.models.SearchResult;
import marquez.api.models.SearchSort;
import marquez.db.DatasetDao;
import marquez.db.JobDao;

/** */
@Slf4j
public class SearchService {
  private final DatasetDao datasetDao;
  private final JobDao jobDao;

  public SearchService(@NonNull final DatasetDao datasetDao, @NonNull final JobDao jobDao) {
    this.jobDao = jobDao;
    this.datasetDao = datasetDao;
  }

  /**
   * @param query
   * @param filter
   * @param sort
   * @param order
   * @param limit
   * @return
   */
  public List<SearchResult> search(
      @NonNull String query,
      @Nullable SearchFilter filter,
      @NonNull SearchSort sort,
      @NonNull SearchOrder order,
      int limit) {
    final ImmutableList.Builder<SearchResult> results = ImmutableList.builder();
    // ..
    if (filter == null) {
      final List<SearchResult> datasetResults = datasetDao.like(query, sort, order, limit);
      results.addAll(datasetResults);
      final List<SearchResult> jobResults = jobDao.like(query, sort, order, limit);
      results.addAll(jobResults);
    } else {
      if (filter == SearchFilter.DATASET) {
        final List<SearchResult> datasetResults = datasetDao.like(query, sort, order, limit);
        results.addAll(datasetResults);
      }
      if (filter == SearchFilter.JOB) {
        final List<SearchResult> jobResults = jobDao.like(query, sort, order, limit);
        results.addAll(jobResults);
      }
    }
    // ..
    return (sort == SearchSort.NAME)
        ? ImmutableList.sortedCopyOf(Comparator.comparing(SearchResult::getName), results.build())
        : ImmutableList.sortedCopyOf(
            Comparator.comparing(SearchResult::getUpdatedAt), results.build());
  }
}
