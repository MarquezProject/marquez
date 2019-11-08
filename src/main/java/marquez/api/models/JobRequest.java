package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(onConstructor = @__(@JsonCreator))
@EqualsAndHashCode
@ToString
public final class JobRequest {
  @Getter private final String type;
  private final List<String> inputs;
  private final List<String> outputs;
  @Nullable private final String location;
  @Nullable private final Map<String, String> context;
  @Nullable private final String description;

  public List<String> getInputs() {
    return ImmutableList.copyOf(new ArrayList<>(inputs));
  }

  public List<String> getOutputs() {
    return ImmutableList.copyOf(new ArrayList<>(outputs));
  }

  public Optional<String> getLocation() {
    return Optional.ofNullable(location);
  }

  public Map<String, String> getContext() {
    return (context == null) ? ImmutableMap.of() : ImmutableMap.copyOf(context);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
