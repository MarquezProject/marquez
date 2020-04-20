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

public enum RunState {
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
