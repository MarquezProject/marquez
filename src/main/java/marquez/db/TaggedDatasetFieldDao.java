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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;
import marquez.common.models.TagName;
import marquez.db.mappers.TaggedDatasetFieldRowMapper;
import marquez.db.models.TaggedDatasetFieldRow;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.BindList;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(TaggedDatasetFieldRowMapper.class)
public interface TaggedDatasetFieldDao {
  @SqlUpdate(
      "INSERT INTO tagged_dataset_fields (dataset_field_uuid, tag_uuid) "
          + "VALUES (:datasetFieldUuid, :tagUuid)")
  void insert(@BindBean TaggedDatasetFieldRow row);

  @SqlQuery(
      "INSERT INTO tagged_dataset_fields (dataset_field_uuid, tag_uuid) "
          + "VALUES (:datasetFieldUuid, :tagUuid) "
          + "RETURNING *")
  Optional<TaggedDatasetFieldRow> insertAndGet(@BindBean TaggedDatasetFieldRow row);

  @SqlQuery(
      "INSERT INTO tagged_dataset_fields (dataset_field_uuid, tag_uuid) "
          + "VALUES ( :datasetFieldUuid, (select uuid from tags where name = :value))"
          + "RETURNING *")
  Optional<TaggedDatasetFieldRow> insert(UUID datasetFieldUuid, @BindBean TagName name);

  @SqlQuery("SELECT * FROM tagged_dataset_fields WHERE dataset_field_uuid = :datasetFieldUuid")
  List<TaggedDatasetFieldRow> find(UUID datasetFieldUuid);

  @SqlQuery("SELECT * FROM tagged_dataset_fields WHERE dataset_field_uuid in (<datasetFieldUuids>)")
  List<TaggedDatasetFieldRow> find(
      @BindList(onEmpty = BindList.EmptyHandling.NULL_STRING) List<UUID> datasetFieldUuids);

  @SqlQuery("SELECT COUNT(*) FROM tagged_dataset_fields")
  Integer count();

  @SqlQuery(
      "SELECT EXISTS (SELECT 1 FROM tagged_dataset_fields WHERE "
          + " dataset_field_uuid = :datasetFieldUuid AND tag_uuid = :tagUuid)")
  boolean exists(UUID datasetFieldUuid, UUID tagUuid);

  @SqlQuery(
      "SELECT EXISTS "
          + " (SELECT 1 FROM datasets AS ds"
          + " INNER JOIN dataset_fields AS df ON df.dataset_uuid = ds.uuid"
          + " INNER JOIN tagged_dataset_fields tfd ON tfd.dataset_field_uuid = df.uuid"
          + " INNER JOIN tags ON tags.uuid = tfd.tag_uuid"
          + " WHERE ds.name=:ds.value"
          + " AND df.name=:dsf.value"
          + " AND tags.name=:t.value)")
  boolean exists(
      @BindBean("ds") DatasetName datasetName,
      @BindBean("dsf") FieldName fieldName,
      @BindBean("t") TagName tagName);

  @SqlUpdate(
      "DELETE FROM tagged_dataset_fields"
          + " WHERE (dataset_field_uuid, tag_uuid)"
          + " IN (SELECT df.uuid, tdf.tag_uuid FROM tagged_dataset_fields AS tdf"
          + " INNER JOIN  dataset_fields AS df ON tdf.dataset_field_uuid = df.uuid"
          + " INNER JOIN tags AS t ON tdf.tag_uuid = t.uuid"
          + " INNER JOIN datasets AS ds ON ds.uuid = df.dataset_uuid"
          + " WHERE ds.name=:ds.value"
          + " AND df.name=:dsf.value"
          + " AND t.name=:t.value)")
  void deleteTagOnField(
      @BindBean("ds") DatasetName datasetName,
      @BindBean("dsf") FieldName fieldName,
      @BindBean("t") TagName tagName);
}
