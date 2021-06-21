package marquez.spark.agent.lifecycle.plan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import scala.runtime.AbstractPartialFunction;

class PlanTraversalTest {

  static class Aggregator implements Consumer<String> {
    List<String> holder = new ArrayList<>();

    @Override
    public void accept(String s) {
      holder.add(s);
    }
  }

  @RequiredArgsConstructor
  static class StringLengthPartialFunction extends AbstractPartialFunction<String, String> {

    private final int processLength;

    @Override
    public boolean isDefinedAt(String x) {
      return processLength == x.length();
    }

    @Override
    public String apply(String value) {
      return value + processLength;
    }
  }

  @Test
  void testBasicPlanTraversal() {
    Aggregator aggregator = new Aggregator();
    Aggregator processedAggregator = new Aggregator();
    PlanTraversal<String, String> planTraversal =
        PlanTraversal.<String, String>builder()
            .processor(new StringLengthPartialFunction(4))
            .listener(aggregator)
            .processedElementListener(processedAggregator)
            .build();

    String processedResult = planTraversal.apply("test");
    String unProcessedResult = planTraversal.apply("testString");

    assertThat(processedResult).isEqualTo("test4");
    assertThat(unProcessedResult).isNull();
    assertThat(aggregator.holder).contains("test", "testString");
    assertThat(processedAggregator.holder).contains("test");
  }

  @Test
  void testBasicPlanTraversalWithoutListeners() {
    PlanTraversal<String, String> planTraversal =
        PlanTraversal.<String, String>builder()
            .processor(new StringLengthPartialFunction(4))
            .build();

    String processedResult = planTraversal.apply("test");
    String unProcessedResult = planTraversal.apply("testString");

    assertThat(processedResult).isEqualTo("test4");
    assertThat(unProcessedResult).isNull();
  }
}
