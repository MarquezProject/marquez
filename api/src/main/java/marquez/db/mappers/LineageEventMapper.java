package marquez.db.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class LineageEventMapper implements RowMapper<LineageEvent> {
  private static final ObjectMapper mapper = Utils.newObjectMapper();

  @Override
  public LineageEvent map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    String eventJson = results.getString("event");
    if (eventJson == null) {
      throw new IllegalArgumentException();
    }

    try {
      return mapper.readValue(eventJson, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException();
    }
  }
}
