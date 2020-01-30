import sys
import pytest
from marquez_client.utils import make_field


def test_make_field():
    columns = [
        'a,NUMBER,description a',
        'b,DECIMAL,description b',
        'c,NUMERIC,description c',
        'd,INT,description d',
        'e,INTEGER,description e',
        'f,BIGINT,description f',
        'g,SMALLINT,description g',
        'h,FLOAT,description h',
        'i,FLOAT4,description i',
        'j,FLOAT8,description j',
        'k,DOUBLE,description k',
        'l,REAL,description l',
        'm,VARCHAR,description m',
        'n,CHAR,description n',
        'o,CHARACTER,description o',
        'p,STRING,description p',
        'q,TEXT,description q',
        'r,BINARY,description r',
        's,VARBINARY,description s',
        't,BOOLEAN,description t',
        'u,DATE,description u',
        'v,DATETIME,description v',
        'w,TIME,description w',
        'x,TIMESTAMP,description x',
        'y,TIMESTAMP_LTZ,description y',
        'z,TIMESTAMP_NTZ,description z',
        'aa,TIMESTAMP_TZ,description aa',
        'bb,VARIANT,description bb',
        'cc,OBJECT,description cc',
        'dd,ARRAY,description dd'
    ]
    fields = []
    for col in columns:
        name, col_type, description = col.split(',', 2)
        description = None if description == 'null' else description
        fields.append(
            make_field(name, col_type, description))

    expected = [
        {'name': 'a', 'type': 'NUMBER', 'description': 'description a'},
        {'name': 'b', 'type': 'DECIMAL', 'description': 'description b'},
        {'name': 'c', 'type': 'NUMERIC', 'description': 'description c'},
        {'name': 'd', 'type': 'INT', 'description': 'description d'},
        {'name': 'e', 'type': 'INTEGER', 'description': 'description e'},
        {'name': 'f', 'type': 'BIGINT', 'description': 'description f'},
        {'name': 'g', 'type': 'SMALLINT', 'description': 'description g'},
        {'name': 'h', 'type': 'FLOAT', 'description': 'description h'},
        {'name': 'i', 'type': 'FLOAT4', 'description': 'description i'},
        {'name': 'j', 'type': 'FLOAT8', 'description': 'description j'},
        {'name': 'k', 'type': 'DOUBLE', 'description': 'description k'},
        {'name': 'l', 'type': 'REAL', 'description': 'description l'},
        {'name': 'm', 'type': 'VARCHAR', 'description': 'description m'},
        {'name': 'n', 'type': 'CHAR', 'description': 'description n'},
        {'name': 'o', 'type': 'CHARACTER', 'description': 'description o'},
        {'name': 'p', 'type': 'STRING', 'description': 'description p'},
        {'name': 'q', 'type': 'TEXT', 'description': 'description q'},
        {'name': 'r', 'type': 'BINARY', 'description': 'description r'},
        {'name': 's', 'type': 'VARBINARY', 'description': 'description s'},
        {'name': 't', 'type': 'BOOLEAN', 'description': 'description t'},
        {'name': 'u', 'type': 'DATE', 'description': 'description u'},
        {'name': 'v', 'type': 'DATETIME', 'description': 'description v'},
        {'name': 'w', 'type': 'TIME', 'description': 'description w'},
        {'name': 'x', 'type': 'TIMESTAMP', 'description': 'description x'},
        {'name': 'y', 'type': 'TIMESTAMP_LTZ', 'description': 'description y'},
        {'name': 'z', 'type': 'TIMESTAMP_NTZ', 'description': 'description z'},
        {'name': 'aa', 'type': 'TIMESTAMP_TZ',
         'description': 'description aa'},
        {'name': 'bb', 'type': 'VARIANT', 'description': 'description bb'},
        {'name': 'cc', 'type': 'OBJECT', 'description': 'description cc'},
        {'name': 'dd', 'type': 'ARRAY', 'description': 'description dd'}
    ]

    assert fields == expected


if __name__ == "__main__":
    pytest.main([sys.argv[0]])
