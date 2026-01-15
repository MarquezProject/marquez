/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Set;
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
  @Produces(MediaType.APPLICATION_JSON)
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
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
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
