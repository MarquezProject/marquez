package marquez.spark.agent.facets;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import marquez.spark.agent.client.LineageEvent;

/**
 * {@link DatasetMetricFacet}s are used to track numeric metrics for the output dataset of a given
 * job run. Depending on the context, a metric may apply to the entire dataset version or only the
 * partition written by the job run. A single {@link DatasetMetricFacet} may contain multiple
 * metrics for a given dataset version. Each metric will reference a single {@link Metric}, possibly
 * containing multiple values, such as {@link Metric#count} or {@link Metric#sum}, or a {@link
 * Quantile} value.
 */
@Data
public class DatasetMetricFacet extends LineageEvent.BaseFacet {
  private final Map<String, Metric> metricValues;

  @Builder
  public DatasetMetricFacet(Map<String, Metric> metricValues) {
    super(
        URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"),
        URI.create(
            "https://github.com/MarquezProject/marquez/blob/main/experimental/integrations/"
                + "marquez-spark-agent/facets/spark-2.4/v1/datasetMetricFacet"));
    this.metricValues = metricValues;
  }

  public DatasetMetricFacet() {
    super(
        URI.create("https://github.com/OpenLineage/OpenLineage/blob/v1-0-0/client"),
        URI.create(
            "https://github.com/MarquezProject/marquez/blob/main/experimental/integrations/"
                + "marquez-spark-agent/facets/spark-2.4/v1/datasetMetricFacet"));
    this.metricValues = new HashMap<>();
  }

  /**
   * Encapsulates a single fact that may be measured in a number of ways- currently, {@link #count},
   * {@link #sum}, and {@link #quantiles} are supported. One or more of these fields can be
   * populated for a single metric.
   */
  @Data
  public static class Metric {
    private final Long count;
    private final Long sum;
    private final List<Quantile> quantiles;

    public Metric(long count, long sum, List<Quantile> quantiles) {
      this.count = count;
      this.sum = sum;
      this.quantiles = quantiles;
    }

    public static Metric count(long value) {
      return new Metric(value, 0L, null);
    }

    public static Metric sum(long value) {
      return new Metric(0L, value, null);
    }
  }

  /**
   * Utility method constructing a single {@link Metric} with a count value. The constructed metric
   * will be added to the current facet.
   *
   * @param name The name of the metric to create
   * @param value The count value of the metric
   */
  public void count(String name, long value) {
    metricValues.put(name, Metric.count(value));
  }

  /**
   * Utility method constructing a single {@link Metric} with a sum value. The constructed metric
   * will be added to the current facet.
   *
   * @param name The name of the metric to create
   * @param value The sum value of the metric
   */
  public void sum(String name, long value) {
    metricValues.put(name, Metric.sum(value));
  }

  /**
   * Utility method constructing a single {@link Metric} with a specified metric value. The
   * constructed metric will be added to the current facet.
   *
   * @param name The name of the metric to create
   * @param metric The
   */
  public void add(String name, Metric metric) {
    metricValues.put(name, metric);
  }

  /**
   * Represents a quantile value- the quantile group (e.g., .20 or .10) and the value for that
   * quantile group.
   */
  public static class Quantile {
    private final double quantile;
    private final double value;

    public Quantile(int quantile, double value) {
      this.quantile = quantile;
      this.value = value;
    }
  }
}
