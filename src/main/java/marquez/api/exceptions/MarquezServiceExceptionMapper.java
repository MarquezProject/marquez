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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import marquez.api.models.ErrorResponse;
import marquez.service.exceptions.MarquezServiceException;

public final class MarquezServiceExceptionMapper
    implements ExceptionMapper<MarquezServiceException> {
  @Override
  public Response toResponse(MarquezServiceException e) {
    return Response.status(INTERNAL_SERVER_ERROR)
        .type(APPLICATION_JSON_TYPE)
        .entity(new ErrorResponse(INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage()))
        .build();
  }
}
