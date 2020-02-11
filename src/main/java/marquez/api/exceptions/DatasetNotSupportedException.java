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
import marquez.common.models.DatasetType;
import marquez.common.models.SourceName;

public final class DatasetNotSupportedException extends BadRequestException {
  private static final long serialVersionUID = 1L;

  public DatasetNotSupportedException(
      final String sourceType, final SourceName sourceName, final DatasetType datasetType) {
    super(
        String.format(
            "Dataset cannot be added to '%s'. Type '%s' not supported for '%s'.",
            checkNotNull(sourceName).getValue(),
            checkNotNull(datasetType),
            checkNotBlank(sourceType)));
  }
}
