package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class JobRequest {
  @Getter @NotEmpty private final String type;
  @Getter @NotEmpty private final List<String> inputs;
  @Getter @NotEmpty private final List<String> outputs;
  @Nullable private final String location;
  @Getter @Nullable private final Map<String, String> context;
  @Nullable private final String description;

  @JsonCreator
  public JobRequest(
      final String type,
      final List<String> inputs,
      final List<String> outputs,
      @Nullable final String location,
      @Nullable final Map<String, String> context,
      @Nullable final String description) {
    this.type = type;
    this.inputs = ImmutableList.copyOf(new ArrayList<>(inputs));
    this.outputs = ImmutableList.copyOf(new ArrayList<>(outputs));
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
