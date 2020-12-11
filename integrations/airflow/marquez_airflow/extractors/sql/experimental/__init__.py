# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

from marquez_airflow.models import DbTableName


class SqlMeta:
    # TODO: Only a single output table may exist, we'll want to rename
    # SqlMeta.out_tables -> SqlMeta.out_table
    def __init__(self, in_tables: [DbTableName], out_tables: [DbTableName]):
        self.in_tables = in_tables
        self.out_tables = out_tables
