/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

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
   * Returns a new {@link ImmutableMap} instance of facets present in the provided {@link
   * java.sql.ResultSet}, or an empty {@link ImmutableMap} if none are present. Note, {@code key}s
   * in the resulting facet map are the facet names (ex: 'schema', 'dataSource', 'documentation',
   * etc).
   */
  static ImmutableMap<String, Object> toFacetsOrNull(
      @NonNull final ResultSet results, String facetsColumn) throws SQLException {
    // Return an empty map of facets if 'facet' column not present.
    if (!Columns.exists(results, facetsColumn)) {
      return ImmutableMap.of();
    }
    // Return a map of facets from the facet array in the result set, or an empty facet map if
    // 'facet' column is null.
    return Optional.ofNullable(stringOrNull(results, facetsColumn))
        .map(
            facetsAsString -> {
              final ObjectNode mergedFacetsAsJson = Utils.getMapper().createObjectNode();
              // Get the array of facets.
              ArrayNode facetsAsJsonArray;
              try {
                facetsAsJsonArray =
                    Utils.fromJson(facetsAsString, new TypeReference<ArrayNode>() {});
              } catch (Exception e) {
                // Log error for malformed facet, then return.
                log.error("Failed to read facets: %s", facetsAsString, e);
                return null;
              }
              // Merge and flatten array of facets; facets are assumed to be in ascending order. As
              // we loop over the facet array, newer facets will be added or override older facets
              // values based on when the OpenLineage event was received. Note, we may want to
              // expand functionality to do deep merge of facet values.
              for (final JsonNode facetsAsJson : facetsAsJsonArray) {
                facetsAsJson
                    .fieldNames()
                    .forEachRemaining(
                        facet -> {
                          final JsonNode facetValueAsJson = facetsAsJson.get(facet);
                          mergedFacetsAsJson.putPOJO(facet, facetValueAsJson);
                        });
              }
              // Before returning, convert the Json object to a map.
              return Utils.getMapper()
                  .convertValue(
                      mergedFacetsAsJson, new TypeReference<ImmutableMap<String, Object>>() {});
            })
        .orElse(ImmutableMap.of());
  }
}
