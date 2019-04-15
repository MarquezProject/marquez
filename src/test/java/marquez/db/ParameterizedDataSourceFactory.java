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

package marquez.db;

import io.dropwizard.db.DataSourceFactory;

public class ParameterizedDataSourceFactory extends DataSourceFactory {

  public ParameterizedDataSourceFactory(
      final String url, final String user, final String driverClass) {
    super();
    this.setUrl(url);
    this.setUser(user);
    this.setDriverClass(driverClass);
  }
}
