package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public final class JobRunState {
  public enum State {
    NEW {
      @Override
      boolean isFinished() {
        return false;
      }
    },
    STARTING {
      @Override
      boolean isFinished() {
        return false;
      }
    },
    RUNNING {
      @Override
      boolean isFinished() {
        return false;
      }
    },
    STOPPING {
      @Override
      boolean isFinished() {
        return false;
      }
    },
    FINISHED {
      @Override
      boolean isFinished() {
        return true;
      }
    },
    FAILED {
      @Override
      boolean isFinished() {
        return true;
      }
    };

    abstract boolean isFinished();
  }

  @NotNull private final Timestamp transitionedAt;
  @NotNull private final UUID jobRunGuid;
  @NotNull private final State state;

  @JsonCreator
  public JobRunState(
      @JsonProperty("transitionedAt") final Timestamp transitionedAt,
      @JsonProperty("jobRunGuid") final UUID jobRunGuid,
      @JsonProperty("state") final State state) {
    this.transitionedAt = transitionedAt;
    this.jobRunGuid = jobRunGuid;
    this.state = state;
  }

  public Timestamp getTransitionedAt() {
    return transitionedAt;
  }

  public UUID getJobRunGuid() {
    return jobRunGuid;
  }

  public State getState() {
    return state;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof JobRunState)) return false;

    final JobRunState other = (JobRunState) o;

    return Objects.equals(transitionedAt, other.transitionedAt)
        && Objects.equals(jobRunGuid, other.jobRunGuid)
        && Objects.equals(state, other.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transitionedAt, jobRunGuid, state);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRunState{");
    sb.append("transitionedAt=").append(transitionedAt);
    sb.append("jobRunGuid=").append(jobRunGuid);
    sb.append("state=").append(state);
    sb.append("}");
    return sb.toString();
  }
}
