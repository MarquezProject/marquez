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
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
import lombok.Value;
import marquez.api.exceptions.DatasetNotFoundException;
import marquez.api.exceptions.DatasetVersionNotFoundException;
import marquez.api.exceptions.FieldNotFoundException;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.api.exceptions.RunNotFoundException;
import marquez.api.exceptions.SourceNotFoundException;
import marquez.api.exceptions.TagNotFoundException;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;
import marquez.common.models.NamespaceName;
import marquez.common.models.RunId;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.common.models.Version;
import marquez.db.SourceDao;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;
import marquez.service.RunService;
import marquez.service.TagService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Dataset;
import marquez.service.models.DatasetMeta;
import marquez.service.models.DatasetVersion;

@Path("/api/v1/namespaces/{namespace}/datasets")
public class DatasetResource {
  private final NamespaceService namespaceService;
  private final DatasetService datasetService;
  private final TagService tagService;
  private final RunService runService;
  private final SourceDao sourceDao;

  public DatasetResource(
      @NonNull final NamespaceService namespaceService,
      @NonNull final DatasetService datasetService,
      @NonNull final TagService tagService,
      @NonNull final RunService runService,
      SourceDao sourceDao) {
    this.namespaceService = namespaceService;
    this.datasetService = datasetService;
    this.tagService = tagService;
    this.runService = runService;
    this.sourceDao = sourceDao;
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("{dataset}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response createOrUpdate(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("dataset") DatasetName datasetName,
      @Valid DatasetMeta datasetMeta)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    datasetMeta.getRunId().ifPresent(this::throwIfNotExists);
    throwIfSourceNotExists(datasetMeta.getSourceName());

    final Dataset dataset = datasetService.createOrUpdate(namespaceName, datasetName, datasetMeta);
    return Response.ok(dataset).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{dataset}")
  @Produces(APPLICATION_JSON)
  public Response get(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("dataset") DatasetName datasetName)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);

    final Dataset dataset =
        datasetService
            .get(namespaceName, datasetName)
            .orElseThrow(() -> new DatasetNotFoundException(datasetName));
    return Response.ok(dataset).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{dataset}/versions/{version}")
  @Produces(APPLICATION_JSON)
  public Response getVersion(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("dataset") DatasetName datasetName,
      @PathParam("version") Version version)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, datasetName);

    final DatasetVersion datasetVersion =
        datasetService
            .getVersion(version)
            .orElseThrow(() -> new DatasetVersionNotFoundException(version));
    return Response.ok(datasetVersion).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Path("{dataset}/versions")
  @Produces(APPLICATION_JSON)
  public Response listVersions(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("dataset") DatasetName datasetName,
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, datasetName);

    final List<DatasetVersion> datasetVersions =
        datasetService.getVersionsFor(namespaceName, datasetName, limit, offset);
    return Response.ok(new DatasetVersions(datasetVersions)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response list(
      @PathParam("namespace") NamespaceName namespaceName,
      @QueryParam("limit") @DefaultValue("100") int limit,
      @QueryParam("offset") @DefaultValue("0") int offset)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);

    final List<Dataset> datasets = datasetService.getAll(namespaceName, limit, offset);
    return Response.ok(new Datasets(datasets)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/{dataset}/tags/{tag}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response tag(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("dataset") DatasetName datasetName,
      @PathParam("tag") TagName tagName)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, datasetName);
    throwIfNotExists(tagName);

    final Dataset dataset = datasetService.tagWith(namespaceName, datasetName, tagName);
    return Response.ok(dataset).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @POST
  @Path("/{dataset}/fields/{field}/tags/{tag}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response tagField(
      @PathParam("namespace") NamespaceName namespaceName,
      @PathParam("dataset") DatasetName datasetName,
      @PathParam("field") FieldName fieldName,
      @PathParam("tag") TagName tagName)
      throws MarquezServiceException {
    throwIfNotExists(namespaceName);
    throwIfNotExists(namespaceName, datasetName);
    throwIfNotExists(namespaceName, datasetName, fieldName);
    throwIfNotExists(tagName);

    final Dataset dataset =
        datasetService.tagFieldWith(namespaceName, datasetName, fieldName, tagName);
    return Response.ok(dataset).build();
  }

  @Value
  static class Datasets {
    @NonNull
    @JsonProperty("datasets")
    List<Dataset> value;
  }

  @Value
  static class DatasetVersions {
    @NonNull
    @JsonProperty("versions")
    List<DatasetVersion> value;
  }

  void throwIfNotExists(@NonNull NamespaceName namespaceName) throws MarquezServiceException {
    if (!namespaceService.exists(namespaceName)) {
      throw new NamespaceNotFoundException(namespaceName);
    }
  }

  void throwIfNotExists(@NonNull NamespaceName namespaceName, @NonNull DatasetName datasetName)
      throws MarquezServiceException {
    if (!datasetService.exists(namespaceName, datasetName)) {
      throw new DatasetNotFoundException(datasetName);
    }
  }

  void throwIfSourceNotExists(SourceName sourceName) throws MarquezServiceException {
    if (!sourceDao.exists(sourceName.getValue())) {
      throw new SourceNotFoundException(sourceName);
    }
  }

  void throwIfNotExists(
      @NonNull NamespaceName namespaceName,
      @NonNull DatasetName datasetName,
      @NonNull FieldName fieldName)
      throws MarquezServiceException {
    if (!datasetService.fieldExists(namespaceName, datasetName, fieldName)) {
      throw new FieldNotFoundException(datasetName, fieldName);
    }
  }

  void throwIfNotExists(@NonNull TagName tagName) throws MarquezServiceException {
    if (!tagService.exists(tagName)) {
      throw new TagNotFoundException(tagName);
    }
  }

  void throwIfNotExists(@NonNull RunId runId) throws MarquezServiceException {
    if (!runService.runExists(runId)) {
      throw new RunNotFoundException(runId);
    }
  }
}
