package marquez.cli;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class SingleJobDAGGenerator {
  public List<JobTemplate> generateGraph(final int numOfInputs, final int numOfOutputs) {
    return Collections.singletonList(
        new JobTemplate(
            IntStream.range(0, numOfInputs).boxed().toList(),
            IntStream.range(numOfInputs, numOfOutputs).boxed().toList(),
            "singleJob",
            Optional.empty()));
  }
}
