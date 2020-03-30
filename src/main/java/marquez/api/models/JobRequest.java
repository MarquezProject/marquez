package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.service.models.DatasetId;

@EqualsAndHashCode
@ToString
public final class JobRequest {
  @Getter @NotEmpty private final String type;
  @Getter private final List<String> inputs;
  @Getter private final List<String> outputs;
  @Getter private final List<DatasetId> inputIds;
  @Getter private final List<DatasetId> outputIds;
  @Nullable private final String location;
  @Getter @Nullable private final Map<String, String> context;
  @Nullable private final String description;

  @JsonCreator
  public JobRequest(
      final String type,
      final List<String> inputs,
      final List<String> outputs,
      final List<DatasetId> inputIds,
      final List<DatasetId> outputIds,
      @Nullable final String location,
      @Nullable final Map<String, String> context,
      @Nullable final String description) {
    this.type = type;
    if ((inputs != null || outputs != null) && (inputIds != null || outputIds != null)) {
      throw new IllegalArgumentException(
          "Either use inputs/outputs or inputIds/outputIds but not both at the same time");
    }
    this.inputs = inputs == null ? null : ImmutableList.copyOf(inputs);
    this.outputs = outputs == null ? null : ImmutableList.copyOf(outputs);
    this.inputIds = inputIds == null ? null : ImmutableList.copyOf(inputIds);
    this.outputIds = outputIds == null ? null : ImmutableList.copyOf(outputIds);
    this.location = location;
    this.context = (context == null) ? ImmutableMap.of() : ImmutableMap.copyOf(context);
    this.description = description;
  }

  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
