package marquez.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.ws.rs.core.Response;
import java.sql.Timestamp;
import java.util.UUID;

public class GetJobRunResponse {
    private final UUID guid;
    private final UUID jobRunDefinitionGuid;
    private final Timestamp endedAt;
    private final Timestamp startedAt;
    private final Timestamp createdAt;
    private final JobRunState.State current_state;

    public GetJobRunResponse(
            @JsonProperty("guid") @NotBlank final UUID guid,
            @JsonProperty Timestamp createdAt,
            @JsonProperty Timestamp startedAt,
            @JsonProperty Timestamp endedAt,
            @JsonProperty UUID jobRunDefinitionGuid,
            @JsonProperty("state") JobRunState.State current_state) {
        this.guid = guid;
        this.createdAt = createdAt;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.jobRunDefinitionGuid = jobRunDefinitionGuid;
        this.current_state = current_state;
    }

    @JsonProperty("created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("started_at")
    public Timestamp getStartedAt() {
        return startedAt;
    }

    @JsonProperty("ended_at")
    public Timestamp getEndedAt() {
        return endedAt;
    }

    @JsonProperty("job_run_definition_guid")
    public UUID getJobRunDefinitionGuid() {
        return jobRunDefinitionGuid;
    }

    @JsonProperty("current_state")
    public String getCurrentState() {
        return current_state.toString();
    }
 }
