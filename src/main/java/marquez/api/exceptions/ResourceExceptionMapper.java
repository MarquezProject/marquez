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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import marquez.api.models.ErrorResponse;

@Provider
public class ResourceExceptionMapper implements ExceptionMapper<ResourceException> {

  @Override
  public Response toResponse(ResourceException exception) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ErrorResponse("Resource exception occurred"))
        .type(APPLICATION_JSON)
        .build();
  }
}
