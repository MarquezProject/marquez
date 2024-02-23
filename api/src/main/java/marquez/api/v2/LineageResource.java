package marquez.api.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.google.common.collect.ImmutableList;
import io.openlineage.server.OpenLineage;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import marquez.db.v2.MetadataDb;

/** ... */
@Slf4j
@Path("/api/v1")
public class LineageResource {
  private final MetadataDb metaDb;

  public LineageResource(@NonNull final MetadataDb metaDb) {
    this.metaDb = metaDb;
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Path("/batch/lineage")
  public Response collectBatch(@NotNull BatchOfEvents batch) {
    metaDb.writeBatchOf(batch.getEvents());
    return Response.ok().build();
  }

  @POST
  @Consumes(APPLICATION_JSON)
  @Path("/lineage")
  public Response collect(@NotNull OpenLineage.RunEvent event) {
    metaDb.write(event);
    return Response.ok().build();
  }

  /** ... */
  @NoArgsConstructor
  @ToString
  static class BatchOfEvents {
    @Getter @Setter ImmutableList<OpenLineage.RunEvent> events;
  }
}
