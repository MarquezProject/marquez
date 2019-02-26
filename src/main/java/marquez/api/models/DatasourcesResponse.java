package marquez.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class DatasourcesResponse {
    @JsonProperty("datasources")
    @Getter
    private List<DatasourceResponse> datasources;
}
