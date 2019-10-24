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

import static com.google.common.base.Charsets.UTF_8;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class Run {
  private static final Joiner.MapJoiner ARGS_JOINER = Joiner.on(":").withKeyValueSeparator("=");

  @NonNull UUID id;
  @NonNull Instant createdAt;
  @NonNull Instant updatedAt;
  @Nullable Instant nominalStartTime;
  @Nullable Instant nominalEndTime;
  @NonNull Run.State state;
  @Nullable Map<String, String> args;

  public enum State {
    NEW,
    RUNNING,
    COMPLETED,
    ABORTED,
    FAILED;
  }

  public Optional<Instant> getNominalStartTime() {
    return Optional.ofNullable(nominalStartTime);
  }

  public Optional<Instant> getNominalEndTime() {
    return Optional.ofNullable(nominalEndTime);
  }

  public Map<String, String> getArgs() {
    return (args == null) ? ImmutableMap.of() : ImmutableMap.copyOf(args);
  }

  public static String checksumFor(final Map<String, String> args) {
    final String argsAsString = ARGS_JOINER.join(args);
    return Hashing.sha256().hashString(argsAsString, UTF_8).toString();
  }
}
