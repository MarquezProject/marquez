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

import static com.google.common.base.Preconditions.checkNotNull;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.util.StdConverter;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import marquez.service.models.Version.UUIDToVersion;
import marquez.service.models.Version.VersionToUUID;

@EqualsAndHashCode
@ToString
@JsonDeserialize(converter = UUIDToVersion.class)
@JsonSerialize(converter = VersionToUUID.class)
public class Version {
  @Getter private final UUID value;

  private Version(final UUID value) {
    checkNotNull(value, "UUID must not be null");
    checkNotBlank(value.toString(), "UUID value must not be blank");
    this.value = value;
  }

  public static Version of(final UUID value) {
    return new Version(value);
  }

  public static class VersionToUUID extends StdConverter<Version, UUID> {
    @Override
    public UUID convert(Version value) {
      return value.getValue();
    }
  }

  public static class UUIDToVersion extends StdConverter<UUID, Version> {
    @Override
    public Version convert(UUID value) {
      return Version.of(value);
    }
  }
}
