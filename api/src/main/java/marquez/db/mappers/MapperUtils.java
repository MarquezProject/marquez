package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
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
  static ImmutableMap<String, Object> toFacetsOrNull(@NonNull final ResultSet results)
      throws SQLException {
    if (!Columns.exists(results, Columns.FACETS)) {
      return null;
    }
    return Optional.ofNullable(stringOrNull(results, Columns.FACETS))
        .map(
            facetsAsString -> {
              final ObjectNode mergedFacetsAsJson = Utils.getMapper().createObjectNode();

              // ...
              ArrayNode facetsAsJsonArray;
              try {
                facetsAsJsonArray =
                    Utils.fromJson(facetsAsString, new TypeReference<ArrayNode>() {});
              } catch (Exception e) {
                // log error ...
                return null;
              }

              // ...
              for (final JsonNode facetsAsJson : facetsAsJsonArray) {
                final JsonNode currFacetsAsJson = facetsAsJson.get("facets");
                // ...
                currFacetsAsJson
                    .fieldNames()
                    .forEachRemaining(
                        facet -> {
                          final JsonNode currFacetValueAsJson = currFacetsAsJson.get(facet);
                          mergedFacetsAsJson.putPOJO(facet, currFacetValueAsJson);
                        });
              }
              return Utils.getMapper()
                  .convertValue(
                      mergedFacetsAsJson, new TypeReference<ImmutableMap<String, Object>>() {});
            })
        .orElse(null);
  }
}
