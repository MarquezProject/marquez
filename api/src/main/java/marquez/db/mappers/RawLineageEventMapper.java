package marquez.db.mappers;

import static marquez.db.Columns.stringOrThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.db.Columns;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
public class RawLineageEventMapper implements RowMapper<JsonNode> {
  @Override
  public JsonNode map(ResultSet rs, StatementContext ctx) throws SQLException {
    String rawEvent = stringOrThrow(rs, Columns.EVENT);

    try {
      ObjectMapper mapper = Utils.getMapper();
      return mapper.readTree(rawEvent);
    } catch (JsonProcessingException e) {
      log.error("Failed to process json", e);
    }
    return null;
  }
}
