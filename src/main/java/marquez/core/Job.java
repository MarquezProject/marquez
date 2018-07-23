package marquez.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import javax.validation.constraints.NotNull;
import java.util.Objects;

public class Job {

    @NotNull
    private final String name;

    @NotNull
    private final Integer currentVersion;

    @NotNull
    private final Timestamp updatedAt;

    @NotNull
    private final Timestamp createdAt;

    @NotNull
    private final Integer ownerId;

    @NotNull
    private final Timestamp nominalTime;

    @NotNull
    private final Boolean isActive;

    @NotNull
    private final String description;

    @JsonCreator
    public Job(@JsonProperty("name") String name,
               @JsonProperty("created_at") Timestamp createdAt,
               @JsonProperty("updated_at") Timestamp updatedAt,
               @JsonProperty("current_version") Integer currentVersion,
               @JsonProperty("owner_id") Integer ownerId,
               @JsonProperty("nominal_time") Timestamp nominalTime,
               @JsonProperty("is_active") Boolean isActive,
               @JsonProperty("description") String description){
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.currentVersion = currentVersion;
        this.ownerId = ownerId;
        this.nominalTime = nominalTime;
        this.isActive = isActive;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActive() {
        return isActive;
    }

    public Timestamp getNominalTime() {
        return nominalTime;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Job)) {
            return false;
        }

        final Job that = (Job) obj;

        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.createdAt, that.createdAt) &&
                Objects.equals(this.currentVersion, that.currentVersion) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.isActive, that.isActive) &&
                Objects.equals(this.nominalTime, that.nominalTime) &&
                Objects.equals(this.ownerId, that.ownerId) &&
                Objects.equals(this.updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, createdAt, currentVersion, description, isActive, nominalTime, ownerId, updatedAt);
    }
}
