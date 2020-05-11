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
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public final class Run {
  @Getter @NonNull private final UUID id;
  @Getter @NonNull private final Instant createdAt;
  @Getter @NonNull private final Instant updatedAt;
  @Nullable private final Instant nominalStartTime;
  @Nullable private final Instant nominalEndTime;
  @Getter @NonNull private final Run.State state;
  @Getter @NonNull private final ImmutableMap<String, String> args;

  public Run(
      @NonNull final UUID id,
      @NonNull final Instant createdAt,
      @NonNull final Instant updatedAt,
      @Nullable final Instant nominalStartTime,
      @Nullable final Instant nominalEndTime,
      @NonNull final Run.State state,
      @Nullable final ImmutableMap<String, String> args) {
    this.id = id;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.nominalStartTime = nominalStartTime;
    this.nominalEndTime = nominalEndTime;
    this.state = state;
    this.args = (args == null) ? ImmutableMap.of() : args;
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
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
  }
}
