package marquez.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

      public Set<State> getValidTransitions() {
        return new HashSet<>(Arrays.asList(State.RUNNING));
      }
    },

    RUNNING {
      @Override
      public boolean isFinished() {
        return false;
      }

      public Set<State> getValidTransitions() {
        return new HashSet<>(
            Arrays.asList(State.RUNNING, State.FAILED, State.COMPLETED, State.ABORTED));
      }
    },

    COMPLETED {
      @Override
      public boolean isFinished() {
        return true;
      }

      public Set<State> getValidTransitions() {
        return Collections.emptySet();
      }
    },
    FAILED {
      @Override
      public boolean isFinished() {
        return true;
      }

      public Set<State> getValidTransitions() {
        return Collections.emptySet();
      }
    },

    ABORTED {
      @Override
      public boolean isFinished() {
        return true;
      }

      public Set<State> getValidTransitions() {
        return Collections.emptySet();
      }
    };

    public abstract boolean isFinished();

    public abstract Set<State> getValidTransitions();

    public static Map<State, Integer> stateToIntMap;
    public static Map<Integer, State> intToStateMap;

    static {
      stateToIntMap = new HashMap();
      stateToIntMap.put(NEW, 0);
      stateToIntMap.put(RUNNING, 1);
      stateToIntMap.put(COMPLETED, 2);
      stateToIntMap.put(FAILED, 3);
      stateToIntMap.put(ABORTED, 4);

      intToStateMap = new HashMap();
      for (State s : stateToIntMap.keySet()) {
        intToStateMap.put(stateToIntMap.get(s), s);
      }
    }

    public static int toInt(State s) {
      return stateToIntMap.get(s);
    }

    public static State fromInt(Integer stateInt) {
      return intToStateMap.get(stateInt);
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
