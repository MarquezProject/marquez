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

import static marquez.common.base.MorePreconditions.checkNotBlank;

import com.google.common.base.Joiner;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public abstract class Urn {
  private static final Joiner ON_URN_DELIM = Joiner.on(UrnPattern.DELIM);

  @Getter private final String value;

  protected Urn(final String value) {
    pattern().throwIfNoMatch(checkNotBlank(value));
    this.value = value;
  }

  protected static String valueFrom(final String namespace, final String... parts) {
    checkNotBlank(namespace);
    for (String part : parts) {
      checkNotBlank(part);
    }
    return ON_URN_DELIM.join(UrnPattern.PREFIX, namespace, ON_URN_DELIM.join(parts));
  }

  public abstract String namespace();

  abstract UrnPattern pattern();
}
