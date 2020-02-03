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

import static com.google.common.base.Preconditions.checkNotNull;
import static marquez.common.base.MorePreconditions.checkNotBlank;

import javax.ws.rs.BadRequestException;
import marquez.common.models.SourceQualifier;

public final class SourceNotSupportedException extends BadRequestException {
  private static final long serialVersionUID = 1L;

  public SourceNotSupportedException(final String type, final SourceQualifier qualifier) {
    super(
        String.format(
            "Source '%s' not supported. "
                + "Please see your configuration to make sure the source has been enabled for '%s'.",
            checkNotBlank(type), checkNotNull(qualifier)));
  }
}
