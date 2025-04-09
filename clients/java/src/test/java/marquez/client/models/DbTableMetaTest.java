/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDbTableMeta;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

@org.junit.jupiter.api.Tag("UnitTests")
public class DbTableMetaTest {
  private static final DatasetMeta META = newDbTableMeta();
  private static final String JSON = JsonGenerator.newJsonFor(META);

  @Test
  public void testToJson() throws JSONException {
    final String actual = META.toJson();
    JSONAssert.assertEquals(JSON, actual, true);
  }
}
