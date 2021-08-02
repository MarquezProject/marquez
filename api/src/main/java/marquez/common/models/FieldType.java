/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.common.models;

import com.google.common.base.Enums;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum FieldType {
  ARRAY,
  BIGINT,
  BINARY,
  BOOL,
  BOOLEAN,
  BPCHAR,
  CHAR,
  CHARACTER,
  DATE,
  DATETIME,
  DECIMAL,
  DOUBLE,
  FLOAT,
  FLOAT4,
  FLOAT8,
  GEOMETRY,
  INT,
  INT2,
  INT4,
  INT8,
  INTEGER,
  LONG,
  NCHAR,
  NUMBER,
  NUMERIC,
  NVARCHAR,
  OBJECT,
  REAL,
  SMALLINT,
  STRING,
  TEXT,
  TIME,
  TIMESTAMP,
  TIMESTAMPTZ,
  TIMESTAMP_LTZ,
  TIMESTAMP_NTZ,
  TIMESTAMP_TZ,
  VARBINARY,
  VARCHAR,
  VARIANT,
  VARYING,
  UNKNOWN;

  private static final Pattern EXTRACT_TYPE = Pattern.compile("(.*)\\(.*\\)");

  public static FieldType fromString(String typeAsString) {
    if (typeAsString == null) return UNKNOWN;
    String type = typeAsString.toUpperCase();
    return Enums.getIfPresent(FieldType.class, type)
        .or(Enums.getIfPresent(FieldType.class, tryToRemoveTypeSize(type)).or(UNKNOWN));
  }

  private static String tryToRemoveTypeSize(String typeAsString) {
    Matcher matcher = EXTRACT_TYPE.matcher(typeAsString);
    return matcher.find() ? matcher.group(1) : typeAsString;
  }
}
