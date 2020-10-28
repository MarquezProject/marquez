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


class DbColumn:
    def __init__(self, name: str, type: str,
                 description: str = None, ordinal_position: int = None):
        self.name = name
        self.type = type
        self.description = description
        self.ordinal_position = ordinal_position

    def __eq__(self, other):
        return self.name == other.name and \
               self.type == other.type and \
               self.description == other.description and \
               self.ordinal_position == other.ordinal_position

    def __repr__(self):
        return f"DbColumn({self.name!r},{self.type!r}, \
                          {self.description!r},{self.ordinal_position!r})"


class DbTableSchema:
    def __init__(self, schema_name: str, table_name: str, columns: [DbColumn]):
        self.schema_name = schema_name
        self.table_name = table_name
        self.columns = columns

    def __eq__(self, other):
        return self.schema_name == other.schema_name and \
               self.table_name == other.table_name and \
               self.columns == other.columns

    def __repr__(self):
        return f"DbTableSchema({self.schema_name!r},{self.table_name!r}, \
                               {self.columns!r})"
