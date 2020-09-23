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

from marquez_airflow.extractors.sql.experimental.parser import SqlParser


def test_parse_simple_select():
    sql_meta = SqlParser.parse(
        '''
        SELECT *
          FROM table0;
        '''
    )

    assert sql_meta.in_tables == ['table0']
    assert sql_meta.out_tables == []


def test_parse_simple_select_into():
    sql_meta = SqlParser.parse(
        '''
        SELECT *
          INTO table0
          FROM table1;
        '''
    )

    assert sql_meta.in_tables == ['table1']
    assert sql_meta.out_tables == ['table0']


def test_parse_simple_join():
    sql_meta = SqlParser.parse(
        '''
        SELECT col0, col1, col2
          FROM table0
          JOIN table1
            ON t1.col0 = t2.col0
        '''
    )

    assert sql_meta.in_tables == ['table0', 'table1']
    assert sql_meta.out_tables == []


def test_parse_simple_inner_join():
    sql_meta = SqlParser.parse(
        '''
        SELECT col0, col1, col2
          FROM table0
         INNER JOIN table1
            ON t1.col0 = t2.col0
        '''
    )

    assert sql_meta.in_tables == ['table0', 'table1']
    assert sql_meta.out_tables == []


def test_parse_simple_left_join():
    sql_meta = SqlParser.parse(
        '''
        SELECT col0, col1, col2
          FROM table0
          LEFT JOIN table1
            ON t1.col0 = t2.col0
        '''
    )

    assert sql_meta.in_tables == ['table0', 'table1']
    assert sql_meta.out_tables == []


def test_parse_simple_left_outer_join():
    sql_meta = SqlParser.parse(
        '''
        SELECT col0, col1, col2
          FROM table0
          LEFT OUTER JOIN table1
            ON t1.col0 = t2.col0
        '''
    )

    assert sql_meta.in_tables == ['table0', 'table1']
    assert sql_meta.out_tables == []


def test_parse_simple_right_join():
    sql_meta = SqlParser.parse(
        '''
        SELECT col0, col1, col2
          FROM table0
          RIGHT JOIN table1
            ON t1.col0 = t2.col0;
        '''
    )

    assert sql_meta.in_tables == ['table0', 'table1']
    assert sql_meta.out_tables == []


def test_parse_simple_right_outer_join():
    sql_meta = SqlParser.parse(
        '''
        SELECT col0, col1, col2
          FROM table0
          RIGHT OUTER JOIN table1
            ON t1.col0 = t2.col0;
        '''
    )

    assert sql_meta.in_tables == ['table0', 'table1']
    assert sql_meta.out_tables == []


def test_parse_simple_insert_into():
    sql_meta = SqlParser.parse(
        '''
        INSERT INTO table0 (col0, col1, col2)
        VALUES (val0, val1, val2);
        '''
    )

    assert sql_meta.in_tables == []
    assert sql_meta.out_tables == ['table0']


def test_parse_simple_insert_into_select():
    sql_meta = SqlParser.parse(
        '''
        INSERT INTO table1 (col0, col1, col2)
        SELECT col0, col1, col2
          FROM table0;
        '''
    )

    assert sql_meta.in_tables == ['table0']
    assert sql_meta.out_tables == ['table1']
