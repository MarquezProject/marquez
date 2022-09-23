package marquez.cli;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record JobTemplate(
    List<Integer> inputs, List<Integer> outputs, String jobName, Optional<ParentRun> parentJob) {

  List<String> getInputDatasetNames() {
    return inputs.stream().map(input -> String.format("dataset%d", input)).toList();
  }

  List<String> getOutputDatasetNames() {
    Optional<String> x = Optional.of("x");

    x.orElse(x.get());
    return outputs.stream().map(output -> String.format("dataset%d", output)).toList();
  }

  String getJobName() {
    return String.format("job%s", jobName);
  }

  public record ParentRun(UUID runId, String jobName, String jobNamespace) {}
}
