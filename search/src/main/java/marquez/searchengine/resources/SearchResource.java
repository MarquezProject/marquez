package marquez.searchengine.resources;

import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.searchengine.SearchConfig;
import marquez.searchengine.services.SearchService;
import marquez.service.models.LineageEvent;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

  private final SearchService searchService;

  public SearchResource(SearchConfig config) throws IOException {
    this.searchService = new SearchService(config);
  }

  @GET
  @Path("/datasets")
  public Response searchDatasets(@QueryParam("query") String query) {
    try {
      return Response.ok(searchService.searchDatasets(query)).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @GET
  @Path("/jobs")
  public Response searchJobs(@QueryParam("query") String query) {
    try {
      return Response.ok(searchService.searchJobs(query)).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @POST
  @Path("/index")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response indexEvent(LineageEvent event) {
    try {
      searchService.indexEvent(event);
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
