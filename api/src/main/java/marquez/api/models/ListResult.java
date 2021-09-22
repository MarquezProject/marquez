package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Map;
import lombok.NonNull;

public class ListResult<T> {

  @NonNull private final Map<String, T> values;

  @JsonProperty("totalCount")
  final int totalCount;

  public ListResult(String propertyName, T data, int totalCount) {
    this.values = setValue(propertyName, data);
    this.totalCount = totalCount;
  }

  @JsonAnySetter
  public Map<String, T> setValue(String key, T value) {
    return Collections.singletonMap(key, value);
  }

  @JsonAnyGetter
  public Map<String, T> getValues() {
    return values;
  }
}
