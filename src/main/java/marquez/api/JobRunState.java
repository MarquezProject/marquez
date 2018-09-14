package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

public final class JobRunState {

  @JsonIgnore private final UUID guid;

  public enum State {
    NEW {
      @Override
      public boolean isFinished() {
        return false;
      }
    },

    RUNNING {
      @Override
      public boolean isFinished() {
        return false;
      }
    },

    COMPLETED {
      @Override
      public boolean isFinished() {
        return true;
      }
    },
    FAILED {
      @Override
      public boolean isFinished() {
        return true;
      }
    },

    ABORTED {
      @Override
      public boolean isFinished() {
        return true;
      }
    };

    public abstract boolean isFinished();

    public static BiMap<State, Integer> mapState;

    static {
      mapState = HashBiMap.create();
      mapState.put(NEW, 0);
      mapState.put(RUNNING, 1);
      mapState.put(COMPLETED, 2);
      mapState.put(FAILED, 3);
      mapState.put(ABORTED, 4);
    }

    public static int toInt(State s) {
      return mapState.get(s);
    }

    public static State fromInt(Integer stateInt) {
      return mapState.inverse().get(stateInt);
    }
  }

  @NotNull private final Timestamp transitionedAt;
  @NotNull private final UUID jobRunGuid;
  @NotNull private final State state;

  @JsonCreator
  public JobRunState(
      final UUID guid,
      @JsonProperty("transitionedAt") final Timestamp transitionedAt,
      @JsonProperty("jobRunGuid") final UUID jobRunGuid,
      @JsonProperty("state") final State state) {
    this.guid = guid;
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

  @JsonIgnore
  public UUID getGuid() {
    return guid;
  }

  @Override
  public int hashCode() {
    return Objects.hash(guid, transitionedAt, jobRunGuid, state);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("JobRunState{");
    sb.append("guid=").append(guid);
    sb.append("transitionedAt=").append(transitionedAt);
    sb.append("jobRunGuid=").append(jobRunGuid);
    sb.append("state=").append(state);
    sb.append("}");
    return sb.toString();
  }
}
