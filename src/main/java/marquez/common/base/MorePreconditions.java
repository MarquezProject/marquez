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

package marquez.common.base;

import static com.google.common.base.Strings.lenientFormat;

import javax.annotation.Nullable;
import lombok.NonNull;

public final class MorePreconditions {
  private MorePreconditions() {}

  public static String checkNotBlank(@NonNull final String arg) {
    if (emptyOrBlank(arg)) {
      throw new IllegalArgumentException();
    }
    return arg;
  }

  public static String checkNotBlank(
      @NonNull final String arg,
      @Nullable final String errorMessage,
      @Nullable final Object... errorMessageArgs) {
    if (emptyOrBlank(arg)) {
      throw new IllegalArgumentException(lenientFormat(errorMessage, errorMessageArgs));
    }
    return arg;
  }

  private static boolean emptyOrBlank(final String arg) {
    return arg.trim().isEmpty();
  }
}
