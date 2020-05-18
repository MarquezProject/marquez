package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class Field {
  @Getter private final String name;
  @Getter private final String type;
  @Getter private final List<String> tags;
  @Nullable String description;

  @JsonCreator
  public Field(
      @NonNull final String name,
      @NonNull final String type,
      @Nullable final List<String> tags,
      @Nullable final String description) {
    this.name = name;
    this.type = type;
    this.tags = (tags == null) ? ImmutableList.of() : ImmutableList.copyOf(tags);
    this.description = description;
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }
}
