package marquez.searchengine.resources;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import marquez.db.OpenLineageDao;
import marquez.searchengine.models.IndexResponse;
import marquez.searchengine.models.SearchRequest;
import marquez.searchengine.models.SearchResult;
import marquez.searchengine.services.SearchService;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

  private final SearchService searchService;

  public SearchResource(Jdbi jdbi) throws IOException {
    OpenLineageDao openLineageDao =
        jdbi.installPlugin(new SqlObjectPlugin()).onDemand(OpenLineageDao.class);
    this.searchService = new SearchService(openLineageDao);
  }

  @POST
  @Path("/jobs/_search")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response searchJobs(SearchRequest request) {
    try {
      String query = request.getQuery().getMulti_match().getQuery();
      List<String> fields = request.getQuery().getMulti_match().getFields();
      SearchResult result = searchService.searchJobs(query, fields);
      // String jsonResponse = new ObjectMapper().writeValueAsString(result);
      return Response.ok(result).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @POST
  @Path("/datasets/_search")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response searchDatasets(SearchRequest request) {
    try {
      String query = request.getQuery().getMulti_match().getQuery();
      List<String> fields = request.getQuery().getMulti_match().getFields();
      SearchResult result = searchService.searchDatasets(query, fields);
      return Response.ok(result).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @PUT
  @Path("/jobs/_doc/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response indexJob(@PathParam("id") String id, Map<String, Object> document) {
    try {
      IndexResponse indexResponse = searchService.indexJobDocument(document);
      return Response.ok(indexResponse).build();
    } catch (IOException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Failed to index job document: " + e.getMessage())
          .build();
    }
  }

  @PUT
  @Path("/datasets/_doc/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response indexDataset(@PathParam("id") String id, Map<String, Object> document) {
    try {
      IndexResponse indexResponse = searchService.indexDatasetDocument(document);
      return Response.ok(indexResponse).build();
    } catch (IOException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Failed to index dataset document: " + e.getMessage())
          .build();
    }
  }

  @GET
  @Path("/ping")
  public Response ping() {
    boolean isHealthy = true;
    if (isHealthy) {
      return Response.ok().entity("{\"status\":\"true\"}").build();
    } else {
      return Response.status(Response.Status.SERVICE_UNAVAILABLE)
          .entity("{\"status\":\"false\"}")
          .build();
    }
  }
}
