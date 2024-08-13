package marquez.searchengine.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import marquez.searchengine.services.SearchService;
import marquez.searchengine.resources.SearchRequest;
import marquez.service.models.LineageEvent;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    private final SearchService searchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchResource() {
        this.searchService = new SearchService();
    }

    @POST
    @Path("/jobs/_search")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response searchJobs(SearchRequest request) {
        try {
            // Extract the actual query and other details from the request object
            String query = request.getQuery().getMulti_match().getQuery();
            List<String> fields = request.getQuery().getMulti_match().getFields();
            String type = request.getQuery().getMulti_match().getType();
            String operator = request.getQuery().getMulti_match().getOperator();
    
            // Log the extracted details for debugging
            System.out.println("Received query: " + query);
            System.out.println("Fields: " + fields);
            System.out.println("Type: " + type);
            System.out.println("Operator: " + operator);
    
            // Perform the search using the extracted query and fields
            SearchService.SearchResult result = searchService.searchJobs(query, fields);
            String jsonResponse = new ObjectMapper().writeValueAsString(result);
            System.out.println("Serialized Response: " + jsonResponse);
            
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
            // Extract the actual query and other details from the request object
            String query = request.getQuery().getMulti_match().getQuery();
            List<String> fields = request.getQuery().getMulti_match().getFields();
            String type = request.getQuery().getMulti_match().getType();
            String operator = request.getQuery().getMulti_match().getOperator();

            // Log the extracted details for debugging
            System.out.println("Received query: " + query);
            System.out.println("Fields: " + fields);
            System.out.println("Type: " + type);
            System.out.println("Operator: " + operator);

            // Perform the search using the extracted query and fields
            SearchService.SearchResult result = searchService.searchDatasets(query, fields);
            String jsonResponse = new ObjectMapper().writeValueAsString(result);
            System.out.println("Serialized Response: " + jsonResponse);

            return Response.ok(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    // Indexing endpoint remains the same
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
