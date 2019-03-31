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

// This test ensures a mapper coverts a row to a service model
package marquez.service.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//import static marquez.common.models.Description.NO_VALUE;
import java.util.UUID;
import lombok.NonNull;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceType;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.DbName;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import marquez.service.models.DbTableVersion;
import marquez.db.models.DatasetRow;
import marquez.db.models.DatasourceRow;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetRowMapperTest {
	private static final UUID uuidRan = UUID.randomUUID();
	private static final DatasourceType DATASOURCE_TYPE = DatasourceType.POSTGRESQL;
	private static final DatasourceRow uuid_DatasourceRow = DatasourceRow.gerVaulue(uuidRan);
	private static final NamespaceName NAMESPACE_NAME = NamespaceName.fromString("test_namepace");
	private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.fromString("test_schema");
	private static final DbTableName DB_TABLE_NAME = DbTableName.fromString("test_table");
	private static final String QUALIFIED_NAME = DB_SCHEMA_NAME.getValue() + '.' + DB_TABLE_NAME.getValue();
	private static final DatasetUrn DATASET_URN = DatasetUrn.from(NAMESPACE_NAME, DatasetName.fromString(QUALIFIED_NAME));
	private static final Description DESCRIPTION = Description.fromString("test description");
	private static final ConnectionUrl CONNECTION_URL =
			ConnectionUrl.fromString(
					String.format(
							"jdbc:%s://localhost:5432/%s",
							DATASOURCE_TYPE.toString().toLowerCase(), DB_TABLE_NAME.getValue()));

	@Test
	public void testMap(){
		final DbTableVersion dbTableVersion =
			DbTableVersion.builder()
				.datasourceUuid(uuid_DatasourceRow)
				.urn(DATASET_URN)
				.description(DESCRIPTION)
				.build();
		final DatasetRow datasetRow = DatasetRowMapper.map(dbTableVersion);
		assertNotNull(NAMESPACE_NAME);
		assertNotNull(uuid_DatasourceRow);
		assertNotNull(CONNECTION_URL.getValue(), DB_SCHEMA_NAME.getValue(), DB_TABLE_NAME.getValue(), DESCRIPTION.getString());
		assertEquals(uuid_DatasourceRow, datasetRow.getDatasourceUuid());
		assertEquals(DATASET_URN, datasetRow.getUrn());
		assertEquals(DESCRIPTION, datasetRow.getDescription());
	}

}
