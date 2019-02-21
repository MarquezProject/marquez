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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class JobRunState {
  private final UUID guid;
  private final Timestamp transitionedAt;
  private final UUID jobRunGuid;
  private final State state;

  public enum State {
    NEW,
    RUNNING,
    COMPLETED,
    FAILED,
    ABORTED;

    static Map<State, Integer> stateToIntMap = new HashMap<State, Integer>();
    static Map<Integer, State> intToStateMap = new HashMap<Integer, State>();

    static {
      stateToIntMap.put(NEW, 0);
      stateToIntMap.put(RUNNING, 1);
      stateToIntMap.put(COMPLETED, 2);
      stateToIntMap.put(FAILED, 3);
      stateToIntMap.put(ABORTED, 4);

      intToStateMap =
          stateToIntMap
              .entrySet()
              .stream()
              .collect(Collectors.toMap(o -> o.getValue(), o -> o.getKey()));
    }

    public static int toInt(State s) {
      return stateToIntMap.get(s);
    }

    public static State fromInt(Integer stateInt) {
      return intToStateMap.get(stateInt);
    }
  }
}
