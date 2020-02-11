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

package marquez.api.exceptions;

import static marquez.common.base.MorePreconditions.checkNotBlank;

import javax.ws.rs.BadRequestException;

public final class FieldNotSupportedException extends BadRequestException {
  private static final long serialVersionUID = 1L;

  public FieldNotSupportedException(final String sourceType, final String fieldType) {
    super(
        String.format(
            "Field type '%s' not supported. "
                + "Please see your configuration to make sure the type has been enabled for '%s'.",
            checkNotBlank(fieldType), checkNotBlank(sourceType)));
  }
}
