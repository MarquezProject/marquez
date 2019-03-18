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

import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = false)
public final class DatasetUrn extends Urn {
  private static final int NUM_COMPONENTS = 2;
  private static final String URN_TYPE = "dataset";
  private static final Pattern REGEX = buildPattern(URN_TYPE, NUM_COMPONENTS);

  public DatasetUrn(@NonNull String value) {
    super(value, REGEX);
  }

  public static DatasetUrn from(@NonNull NamespaceName namespace, @NonNull DatasetName dataset) {
    final String value = fromComponents(URN_TYPE, namespace.getValue(), dataset.getValue());
    return fromString(value);
  }

  public static DatasetUrn fromString(String value) {
    return new DatasetUrn(value);
  }
}
