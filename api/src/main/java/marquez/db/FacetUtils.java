/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;
import marquez.common.Utils;
import org.postgresql.util.PGobject;

public class FacetUtils {

  static ObjectNode asJson(@NonNull final String facetName, @NonNull Object facetValue) {
    final ObjectNode facetAsJson = Utils.getMapper().createObjectNode();
    facetAsJson.putPOJO(facetName, facetValue);
    return facetAsJson;
  }

  static PGobject toPgObject(String name, Object o) {
    return Columns.toPgObject(asJson(name, o));
  }
}
