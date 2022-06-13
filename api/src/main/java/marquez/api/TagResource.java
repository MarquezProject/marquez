/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import marquez.service.ServiceFactory;
import marquez.service.models.Tag;

@Path("/api/v1/tags")
public class TagResource extends BaseResource {
  public TagResource(@NonNull final ServiceFactory serviceFactory) {
    super(serviceFactory);
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @GET
  @Produces(APPLICATION_JSON)
  public Response list(
      @QueryParam("limit") @DefaultValue("100") @Min(value = 0) int limit,
      @QueryParam("offset") @DefaultValue("0") @Min(value = 0) int offset) {
    final Set<Tag> tags = tagService.findAll(limit, offset);
    return Response.ok(new Tags(tags)).build();
  }

  @Timed
  @ResponseMetered
  @ExceptionMetered
  @PUT
  @Path("/{name}")
  @Consumes(APPLICATION_JSON)
  @Produces(APPLICATION_JSON)
  public Response create(@PathParam("name") String name, TagDescription description) {
    Tag tag = new Tag(name, description.getValue());
    Tag upsertedTag = tagService.upsert(tag);
    return Response.ok(upsertedTag).build();
  }

  @Value
  static class TagDescription {
    @Getter String value;

    @JsonCreator
    TagDescription(@JsonProperty("description") final String value) {
      this.value = value;
    }
  }

  @Value
  static class Tags {
    @NonNull
    @JsonProperty("tags")
    Set<Tag> value;
  }
}
