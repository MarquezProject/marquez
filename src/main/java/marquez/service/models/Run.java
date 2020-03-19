/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.service.models;

import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class Run {
  @NonNull UUID id;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @Nullable Instant nominalStartTime;
  @Nullable Instant nominalEndTime;
  @NonNull Run.State state;
  @Nullable Instant startedAt;
  @Nullable Instant endedAt;
  @Nullable Map<String, String> args;

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Optional<Instant> getStartedAt() {
    return Optional.ofNullable(startedAt);
  }

  public Optional<Instant> getEndedAt() {
    return Optional.ofNullable(endedAt);
  }

  public Map<String, String> getArgs() {
    return (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
  }

  public enum State {
    NEW {
      @Override
      public boolean isComplete() {
        return false;
      }
    },
    RUNNING {
      @Override
      public boolean isComplete() {
        return false;
      }

      @Override
      public boolean isStarting() {
        return true;
      }
    },
    COMPLETED {
      @Override
      public boolean isComplete() {
        return true;
      }
    },
    ABORTED {
      @Override
      public boolean isComplete() {
        return false;
      }
    },
    FAILED {
      @Override
      public boolean isComplete() {
        return false;
      }
    };

    /** Returns true if this state is complete. */
    public abstract boolean isComplete();

    /** Returns true if this state is Running. */
    public boolean isStarting() {
      return false;
    }
  }
}
