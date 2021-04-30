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
   * Returns a new {@link ImmutableMap} instance of facets present in the provided
   * {java.sql.ResultSet}. the keys are the facet names
   */
  static ImmutableMap<String, Object> toFacetsOrNull(@NonNull final ResultSet results)
      throws SQLException {
    // ...
    if (!Columns.exists(results, Columns.FACETS)) {
      return ImmutableMap.of();
    }
    return Optional.ofNullable(stringOrNull(results, Columns.FACETS))
        .map(
            facetsAsString -> {
              // ...
              final ObjectNode mergedFacetsAsJson = Utils.getMapper().createObjectNode();

              // Get the array of facets.
              ArrayNode facetsAsJsonArray;
              try {
                facetsAsJsonArray =
                    Utils.fromJson(facetsAsString, new TypeReference<ArrayNode>() {});
              } catch (Exception e) {
                // Log error, then return
                log.error("Failed to read facets: %s", facetsAsString, e);
                return null;
              }

              // NOTE: does not do a deep merge on facets
              // Merge array of facets.
              for (final JsonNode facetsAsJson : facetsAsJsonArray) {
                facetsAsJson
                    .fieldNames()
                    .forEachRemaining(
                        facet -> {
                          final JsonNode facetValueAsJson = facetsAsJson.get(facet);
                          mergedFacetsAsJson.putPOJO(facet, facetValueAsJson);
                        });
              }
              return Utils.getMapper()
                  .convertValue(
                      mergedFacetsAsJson, new TypeReference<ImmutableMap<String, Object>>() {});
            })
        .orElse(ImmutableMap.of());
  }
}
