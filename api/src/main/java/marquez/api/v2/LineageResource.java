package marquez.api.v2;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.collect.ImmutableList;
import io.openlineage.server.OpenLineage;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import marquez.db.v2.MetadataDb;

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
  public Response collectBatchOf(@NotNull BatchOfEvents batch) {
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

  /** A batch of lineage {@code events} . */
  @EqualsAndHashCode
  @ToString
  static class BatchOfEvents {
    @Getter final List<OpenLineage.RunEvent> events;

    @JsonCreator
    BatchOfEvents(@NonNull final ImmutableList<OpenLineage.RunEvent> events) {
      this.events = events;
    }
  }
}
