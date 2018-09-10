package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.sql.Timestamp;
import java.util.UUID;

public class GetJobRunRequest {

    final UUID guid;

    @JsonCreator
    public GetJobRunRequest(
            @JsonProperty("id") final UUID guid) {
        this.guid = guid;
    }
    public UUID getGuid() {
        return this.guid;
    }
}
