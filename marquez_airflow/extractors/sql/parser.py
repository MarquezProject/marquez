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

import logging
import re
from enum import Enum

import sqlparse
from sqlparse.sql import TokenList
from sqlparse.tokens import Literal, Name, Punctuation

log = logging.getLogger(__name__)


TABLE_REGEX = re.compile(
    r'(((LEFT\s+|RIGHT\s+|FULL\s+)?(INNER\s+|OUTER\s+|STRAIGHT\s+)?'
    r'|(CROSS\s+|NATURAL\s+)?)?JOIN$)|(FROM$)',
    re.IGNORECASE | re.UNICODE)


class State(Enum):
    SEARCHING = 1
    FINDING_TABLE = 2
    BUILDING_TABLE = 3
    FINDING_ALIAS = 4
    BUILDING_ALIAS = 5
    CHECKING_ALIAS = 6


class SqlParser:

    @staticmethod
    def is_table_keyword(t):
        return t.is_keyword and TABLE_REGEX.match(t.normalized)

    @staticmethod
    def get_tables(statement):

        aliases = []
        tables = []

        parsed = sqlparse.parse(statement)
        if not parsed or len(parsed) == 0:
            return set()

        # flatten the identifiers
        tokens = [t for t in TokenList(parsed[0].tokens).flatten()]

        curr_name = ''
        alias_candidate = ''
        last_non_whitespace = None

        _state = State.SEARCHING
        for t in tokens:

            if _state == State.SEARCHING:
                if SqlParser.is_table_keyword(t):
                    _state = State.FINDING_TABLE
                if (t.is_keyword and t.normalized == 'AS'
                        and last_non_whitespace.ttype is Name):
                    alias_candidate = last_non_whitespace
                    _state = State.CHECKING_ALIAS

            elif _state == State.FINDING_TABLE:
                if t.ttype is Name or t.ttype is Literal.String.Symbol:
                    curr_name += t.value
                    _state = State.BUILDING_TABLE
                elif t.ttype is Punctuation or t.is_keyword:
                    _state = State.SEARCHING

            elif _state == State.BUILDING_TABLE:
                if t.ttype is Name or t.ttype is Literal.String.Symbol or (
                        t.value == '.'):
                    curr_name += t.value
                else:
                    if curr_name not in tables:
                        tables.append(curr_name)
                    curr_name = ''
                    if t.is_whitespace:
                        _state = State.FINDING_ALIAS
                    else:
                        _state = State.SEARCHING

            elif _state == State.FINDING_ALIAS:
                if t.ttype is Name:
                    curr_name += t.value
                    _state = State.BUILDING_ALIAS
                elif SqlParser.is_table_keyword(t):
                    _state = State.FINDING_TABLE
                elif t.ttype is Punctuation or t.is_keyword:
                    _state = State.SEARCHING

            elif _state == State.BUILDING_ALIAS:
                if t.ttype is Name or t.value == '.':
                    curr_name += t.value
                else:
                    aliases.append(curr_name)
                    curr_name = ''
                    if SqlParser.is_table_keyword(t):
                        _state = State.FINDING_TABLE
                    else:
                        _state = State.SEARCHING

            elif _state == State.CHECKING_ALIAS:
                if SqlParser.is_table_keyword(t):
                    _state = State.FINDING_TABLE
                elif not t.is_whitespace:
                    if t.value == '(':
                        aliases.append(alias_candidate.value)
                    _state = State.SEARCHING

            if not t.is_whitespace:
                last_non_whitespace = t

        # Naive way to get rid of aliases
        tables = [t for t in tables if t.split('.')[0] not in aliases]
        return tables
