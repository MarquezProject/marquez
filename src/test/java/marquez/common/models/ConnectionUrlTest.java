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

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class ConnectionUrlTest {
  private static final DataSource DATA_SOURCE = DataSource.fromString("postgresql");
  private static final int DB_PORT = 5432;
  private static final DbName DB_NAME = DbName.fromString("test");

  @Test
  public void testNewConnectionUrl() {
    final String rawValue =
        String.format(
            "jdbc:%s://localhost:%d/%s", DATA_SOURCE.getValue(), DB_PORT, DB_NAME.getValue());
    final ConnectionUrl connectionUrl = ConnectionUrl.fromString(rawValue);
    assertEquals(DATA_SOURCE, connectionUrl.getDataSource());
    assertEquals(DB_NAME, connectionUrl.getDbName());
    assertEquals(rawValue, connectionUrl.getRawValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewConnectionUrl_throwsException_onNullRawValue() {
    final String nullRawValue = null;
    ConnectionUrl.fromString(nullRawValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onEmptyRawValue() {
    final String emptyRawValue = "";
    ConnectionUrl.fromString(emptyRawValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onBlankRawValue() {
    final String blankRawValue = " ";
    ConnectionUrl.fromString(blankRawValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onUnknownProtocolValue() {
    final String unknownProtocolValue =
        String.format("foo:postgresql://localhost:%d/%s", DB_PORT, DB_NAME.getValue());
    ConnectionUrl.fromString(unknownProtocolValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onMissingPartValue() {
    final String missingPartValue =
        String.format("jdbc:postgresql://localhost/%s", DB_NAME.getValue());
    ConnectionUrl.fromString(missingPartValue);
  }
}
