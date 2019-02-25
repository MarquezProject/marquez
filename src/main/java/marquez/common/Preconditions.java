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

package marquez.common;

import javax.annotation.Nullable;

public final class Preconditions {
  private Preconditions() {}

  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  public static <T> T checkNotNull(T reference, @Nullable String errorMessage) {
    if (reference == null) {
      throw new NullPointerException(errorMessage);
    }
    return reference;
  }

  public static String checkNotBlank(String argument) {
    checkNotNull(argument);
    if (argument.trim().isEmpty()) {
      throw new IllegalArgumentException();
    }
    return argument;
  }

  public static String checkNotBlank(String argument, @Nullable String errorMessage) {
    checkNotNull(argument);
    if (argument.trim().isEmpty()) {
      throw new IllegalArgumentException(errorMessage);
    }
    return argument;
  }

  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  public static void checkArgument(boolean expression, @Nullable String errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(errorMessage);
    }
  }
}
