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

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.api.exceptions.DatasetNotFoundException;
import marquez.api.exceptions.FieldNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.exceptions.RunNotFoundException;
import marquez.api.exceptions.RunNotValidException;
import marquez.api.exceptions.TagNotFoundException;
import marquez.api.mappers.Mapper;
import marquez.api.models.DatasetRequest;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.Utils;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;
import marquez.service.DatasetService;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;

@Slf4j
@Path("/api/v1/namespaces/{namespace}/datasets")
public final class DatasetResource {
  private final NamespaceService namespaceService;
  private final DatasetService datasetService;
  private final JobService jobService;
  private final TagService tagService;

  public DatasetResource(
      @NonNull final NamespaceService namespaceService,
      @NonNull final DatasetService datasetService,
      @NonNull final JobService jobService,
      @NonNull final TagService tagService) {
    this.namespaceService = namespaceService;
    this.datasetService = datasetService;
    this.jobService = jobService;
    this.tagService = tagService;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("{dataset}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(
      @PathParam("namespace") String namespaceString,
      @PathParam("dataset") String datasetString,
      @Valid DatasetRequest request)
      throws MarquezServiceException {
    log.debug("Request: {}", request);
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);
    throwIfNotExists(request.getRunId().map(this::toRunIdOrThrow).orElse(null));

    final DatasetName datasetName = DatasetName.of(datasetString);
    final DatasetMeta datasetMeta = Mapper.toDatasetMeta(request);
    final Dataset dataset = datasetService.createOrUpdate(namespaceName, datasetName, datasetMeta);
    final DatasetResponse response = Mapper.toDatasetResponse(dataset);
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{dataset}")
  @Produces(APPLICATION_JSON)
  public Response get(
      @PathParam("namespace") String namespaceString, @PathParam("dataset") String datasetString)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);

    final DatasetName datasetName = DatasetName.of(datasetString);
    final DatasetResponse response =
        datasetService
            .get(namespaceName, datasetName)
            .map(Mapper::toDatasetResponse)
            .orElseThrow(() -> new DatasetNotFoundException(datasetName));
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response list(
      @PathParam("namespace") String namespaceString,
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);

    final List<Dataset> datasets = datasetService.getAll(namespaceName, limit, offset);
    final DatasetsResponse response = Mapper.toDatasetsResponse(datasets);
    log.debug("Response: {}", response);
    return Response.ok(response).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/{dataset}/tags/{tag}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response tag(
      @PathParam("namespace") String namespaceString,
      @PathParam("dataset") String datasetString,
      @PathParam("tag") String tagName)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);
    final DatasetName datasetName = DatasetName.of(datasetString);
    throwIfNotExists(namespaceName, datasetName);
    throwIfNotExists(tagName);

    datasetService.tagWith(namespaceName, datasetName, tagName);
    return get(namespaceString, datasetString);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/{dataset}/fields/{field}/tags/{tag}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response tag(
      @PathParam("namespace") String namespaceString,
      @PathParam("dataset") String datasetString,
      @PathParam("field") String fieldName,
      @PathParam("tag") String tagName)
      throws MarquezServiceException {
    final NamespaceName namespaceName = NamespaceName.of(namespaceString);
    throwIfNotExists(namespaceName);
    final DatasetName datasetName = DatasetName.of(datasetString);
    throwIfNotExists(namespaceName, datasetName);
    throwIfNotExists(namespaceName, datasetName, fieldName);
    throwIfNotExists(tagName);

    datasetService.tagWith(namespaceName, datasetName, fieldName, tagName);
    return get(namespaceString, datasetString);
  }

  private void throwIfNotExists(@NonNull NamespaceName namespaceName)
      throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }

  private void throwIfNotExists(
      @NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName)
      throws MarquezServiceException {
    if (!datasetService.exists(namespaceName, datasetName)) {
      throw new DatasetNotFoundException(datasetName);
    }
  }

  private void throwIfNotExists(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull String fieldName)
      throws MarquezServiceException {
    if (!datasetService.exists(namespaceName, datasetName, fieldName)) {
      throw new FieldNotFoundException(datasetName, fieldName);
    }
  }

  private void throwIfNotExists(@NonNull String tagName) throws MarquezServiceException {
    if (!tagService.exists(tagName)) {
      throw new TagNotFoundException(tagName);
    }
  }

  private void throwIfNotExists(@Nullable UUID runId) throws MarquezServiceException {
    if (runId != null) {
      if (!jobService.runExists(runId)) {
        throw new RunNotFoundException(runId);
      }
    }
  }

  private UUID toRunIdOrThrow(@NonNull String runIdString) {
    try {
      return Utils.toUuid(runIdString);
    } catch (IllegalArgumentException e) {
      throw new RunNotValidException(runIdString);
    }
  }
}
