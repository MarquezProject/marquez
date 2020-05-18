package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class Field {
  String name;
  String type;
  @Nullable List<String> tags;
  @Nullable String description;

  @JsonCreator
  public Field(
      @NonNull final String name,
      @NonNull final String type,
      @Nullable final List<String> tags,
      @Nullable final String description) {
    this.name = name;
    this.type = type;
    this.tags = ImmutableList.copyOf(new ArrayList<>(tags));
    this.description = description;
  }

  public List<String> getTags() {
    return tags;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
