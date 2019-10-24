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

package marquez;

import org.testcontainers.containers.PostgreSQLContainer;

public class MarquezDb extends PostgreSQLContainer<MarquezDb> {
  private static final String POSTGRES_9_6 = "postgres:9.6";
  private static MarquezDb db;

  private MarquezDb() {
    super(POSTGRES_9_6);
  }

  public static MarquezDb create() {
    if (db == null) {
      db = new MarquezDb();
    }
    return db;
  }

  @Override
  public void start() {
    super.start();

    System.setProperty("DB_URL", db.getJdbcUrl());
    System.setProperty("DB_USERNAME", db.getUsername());
    System.setProperty("DB_PASSWORD", db.getPassword());
  }

  @Override
  public void stop() {}
}
