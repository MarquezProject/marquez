package marquez.api.resources;


import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.ResponseMetered;
import com.codahale.metrics.annotation.Timed;
import lombok.NonNull;
import marquez.api.models.DatasourcesResponse;
import marquez.api.models.NamespacesResponse;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.Collections;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/api/v1")
public final class DatasourceResource {

    public DatasourceResource() {
    }

    @GET
    @ResponseMetered
    @ExceptionMetered
    @Timed
    @Path("/datasources")
    @Produces(APPLICATION_JSON)
    public Response list(
            @QueryParam("limit") @DefaultValue("100") Integer limit,
            @QueryParam("offset") @DefaultValue("0") Integer offset) {
        return Response.ok(new DatasourcesResponse(Collections.emptyList())).build();
    }
}