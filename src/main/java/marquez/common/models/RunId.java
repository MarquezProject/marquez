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

package marquez.common.models;

import static com.google.common.base.Preconditions.checkNotNull;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.common.models.RunId.RunIdToUUID;
import marquez.common.models.RunId.UUIDToRunId;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = UUIDToRunId.class)
@JsonSerialize(converter = RunIdToUUID.class)
public class RunId {
  @Getter private final UUID value;

  private RunId(final UUID value) {
    checkNotNull(value, "UUID must not be null");
    checkNotBlank(value.toString(), "UUID value must not be blank");
    this.value = value;
  }

  public static RunId of(final UUID value) {
    return new RunId(value);
  }

  public static class RunIdToUUID extends StdConverter<RunId, UUID> {
    @Override
    public UUID convert(RunId value) {
      return value.getValue();
    }
  }

  public static class UUIDToRunId extends StdConverter<UUID, RunId> {
    @Override
    public RunId convert(UUID value) {
      return RunId.of(value);
    }
  }
}
