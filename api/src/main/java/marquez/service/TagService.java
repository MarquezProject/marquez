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

package marquez.service;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.db.BaseDao;
import marquez.service.models.Tag;

@Slf4j
public class TagService extends DelegatingDaos.DelegatingTagDao {

  public TagService(@NonNull final BaseDao dao) {
    super(dao.createTagDao());
  }

  public void init(@NonNull ImmutableSet<Tag> tags) {
    for (final Tag tag : tags) {
      upsert(tag);
    }
  }
}
