package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.Facets;
import marquez.db.Columns;

@Slf4j
@UtilityClass
public final class MapperUtils {

  static Set<String> getColumnNames(ResultSetMetaData metaData) {
    try {
      Set<String> columns = new HashSet<>();
      for (int i = 1; i <= metaData.getColumnCount(); i++) {
        columns.add(metaData.getColumnName(i));
      }
      return columns;
    } catch (SQLException e) {
      log.error("Unable to get column names", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a new {@link Facets} instance of type {com.fasterxml.jackson.databind.JsonNode}, or
   * {@code null} if not present in {java.sql.ResultSet}.
   */
  static Facets toFacetsOrNull(@NonNull final ResultSet results) throws SQLException {
    if (!Columns.exists(results, Columns.FACETS)) {
      return null;
    }
    return Optional.ofNullable(stringOrNull(results, Columns.FACETS))
        .map(
            facetsAsString -> {
              final JsonNode facetsAsJson =
                  Utils.fromJson(facetsAsString, new TypeReference<JsonNode>() {});
              return Facets.of(facetsAsJson);
            })
        .orElse(null);
  }
}
