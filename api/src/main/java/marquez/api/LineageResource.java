/*
 *
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

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import graphql.ExecutionInput;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.service.ServiceFactory;

@Path("/api/v1")
@Slf4j
public class LineageResource extends BaseResource {
  private final GraphQL graphql;

  public LineageResource(@NonNull final ServiceFactory serviceFactory, GraphQLSchema schema) {
    super(serviceFactory);
    this.graphql = GraphQL.newGraphQL(schema).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("/lineageGraph")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public void createOrUpdate(
      @QueryParam("name") String name,
      @QueryParam("namespace") String namespace,
      @QueryParam("depth") @DefaultValue("10") int depth,
      @Suspended final AsyncResponse asyncResponse) {
    ExecutionInput lineageQuery =
        ExecutionInput.newExecutionInput()
            .query(
                ""
                    + "query getLineage($name: String!, $namespace: String!, $depth: Int!) {\n"
                    + "  lineageFromJob(\n"
                    + "      name: $name, \n"
                    + "      namespace: $namespace,\n"
                    + "      depth: $depth){\n"
                    + "    graph {\n"
                    + "      ... on DatasetLineageEntry {\n"
                    + "        name\n"
                    + "        namespace\n"
                    + "        type\n"
                    + "        data {\n"
                    + "          name\n"
                    + "          physicalName\n"
                    + "          fields {\n"
                    + "            name\n"
                    + "          }\n"
                    + "        }\n"
                    + "        inEdges {\n"
                    + "          name\n"
                    + "          namespace\n"
                    + "          type\n"
                    + "        }\n"
                    + "        outEdges {\n"
                    + "          name\n"
                    + "          namespace\n"
                    + "          type\n"
                    + "        }\n"
                    + "      }\n"
                    + "      ... on JobLineageEntry {\n"
                    + "        name\n"
                    + "        namespace\n"
                    + "        type\n"
                    + "        data {\n"
                    + "          name\n"
                    + "          currentVersion {\n"
                    + "            version\n"
                    + "            location\n"
                    + "          }\n"
                    + "        }\n"
                    + "        inEdges {\n"
                    + "          name\n"
                    + "          namespace\n"
                    + "        }\n"
                    + "        outEdges {\n"
                    + "          name\n"
                    + "          namespace\n"
                    + "        }\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }\n"
                    + "}")
            .variables(ImmutableMap.of("name", name, "namespace", namespace, "depth", depth))
            .build();

    graphql
        .executeAsync(lineageQuery)
        .whenComplete(
            (result, err) -> {
              if (err != null) {
                log.error("Unexpected error while processing request", err);
                asyncResponse.resume(Response.status(INTERNAL_SERVER_ERROR).build());
              } else {
                asyncResponse.resume(Response.ok(result).build());
              }
            });
  }
}
