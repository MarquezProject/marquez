package marquez.spark.agent.lifecycle.plan;

import java.util.List;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Singular;
import scala.PartialFunction;
import scala.runtime.AbstractPartialFunction;

@Builder
public class PlanTraversal<T, R> extends AbstractPartialFunction<T, R> {

  PartialFunction<T, R> processor;
  @Singular List<Consumer<T>> listeners;
  @Singular List<Consumer<T>> processedElementListeners;

  @Override
  public R apply(T element) {
    listeners.forEach(l -> l.accept(element));
    if (processor.isDefinedAt(element)) {
      R res = processor.apply(element);
      processedElementListeners.forEach(c -> c.accept(element));
      return res;
    }
    return null;
  }

  @Override
  public boolean isDefinedAt(T x) {
    return processor.isDefinedAt(x);
  }
}
