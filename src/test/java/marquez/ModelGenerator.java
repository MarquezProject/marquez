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

package marquez;

import java.time.Instant;
import java.util.Random;

public abstract class ModelGenerator {
  private static final Random RANDOM = new Random();

  public static Instant newTimestamp() {
    return Instant.now();
  }

  protected static Integer newId() {
    return RANDOM.nextInt(Integer.MAX_VALUE - 1);
  }

  protected static Integer newIdWithBound(final int bound) {
    return RANDOM.nextInt(bound);
  }
}
