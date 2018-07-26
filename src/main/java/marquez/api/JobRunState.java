package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Objects;
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
  @NotNull private final long jobRunId;
  @NotNull private final State state;

  @JsonCreator
  public JobRunState(
      @JsonProperty("transitioned_at") final Timestamp transitionedAt,
      @JsonProperty("job_run_id") final long jobRunId,
      @JsonProperty("state") final State state) {
    this.transitionedAt = transitionedAt;
    this.jobRunId = jobRunId;
    this.state = state;
  }

  public Timestamp getTransitionedAt() {
    return transitionedAt;
  }

  public long getJobRunId() {
    return jobRunId;
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
        && Objects.equals(jobRunId, other.jobRunId)
        && Objects.equals(state, other.state);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transitionedAt, jobRunId, state);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRunState{");
    sb.append("transitionedAt=").append(transitionedAt);
    sb.append("jobRunId=").append(jobRunId);
    sb.append("state=").append(state);
    sb.append("}");
    return sb.toString();
  }
}
