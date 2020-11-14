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

package marquez.client.models;

public enum RunState {
  NEW,
  RUNNING,
  COMPLETED,
  ABORTED,
  FAILED;

  /* Returns true if this state is complete. */
  public boolean isComplete() {
    return this == COMPLETED;
  }

  /* Returns true if this state is running. */
  public boolean isStarting() {
    return this == RUNNING;
  }
}
