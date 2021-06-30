"""Unit test utility functions.

Note that all imports should be inside the functions to avoid import/mocking
issues.
"""
import string
import os
from unittest import mock
from unittest import TestCase

import agate
import pytest
from dbt.dataclass_schema import ValidationError


def normalize(path):
    """On windows, neither is enough on its own:

    >>> normcase('C:\\documents/ALL CAPS/subdir\\..')
    'c:\\documents\\all caps\\subdir\\..'
    >>> normpath('C:\\documents/ALL CAPS/subdir\\..')
    'C:\\documents\\ALL CAPS'
    >>> normpath(normcase('C:\\documents/ALL CAPS/subdir\\..'))
    'c:\\documents\\all caps'
    """
    return os.path.normcase(os.path.normpath(path))


class Obj:
    which = 'blah'
    single_threaded = False


def mock_connection(name, state='open'):
    conn = mock.MagicMock()
    conn.name = name
    conn.state = state
    return conn


def profile_from_dict(profile, profile_name, cli_vars='{}'):
    from dbt.config import Profile
    from dbt.config.renderer import ProfileRenderer
    from dbt.context.base import generate_base_context
    from dbt.config.utils import parse_cli_vars
    if not isinstance(cli_vars, dict):
        cli_vars = parse_cli_vars(cli_vars)

    renderer = ProfileRenderer(generate_base_context(cli_vars))
    return Profile.from_raw_profile_info(
        profile,
        profile_name,
        renderer,
    )


def project_from_dict(project, profile, packages=None, selectors=None, cli_vars='{}'):
    from dbt.context.target import generate_target_context
    from dbt.config import Project
    from dbt.config.renderer import DbtProjectYamlRenderer
    from dbt.config.utils import parse_cli_vars
    if not isinstance(cli_vars, dict):
        cli_vars = parse_cli_vars(cli_vars)

    renderer = DbtProjectYamlRenderer(generate_target_context(profile, cli_vars))

    project_root = project.pop('project-root', os.getcwd())

    return Project.render_from_dict(
            project_root, project, packages, selectors, renderer
        )


def config_from_parts_or_dicts(project, profile, packages=None, selectors=None, cli_vars='{}'):
    from dbt.config import Project, Profile, RuntimeConfig
    from copy import deepcopy

    if isinstance(project, Project):
        profile_name = project.profile_name
    else:
        profile_name = project.get('profile')

    if not isinstance(profile, Profile):
        profile = profile_from_dict(
            deepcopy(profile),
            profile_name,
            cli_vars,
        )

    if not isinstance(project, Project):
        project = project_from_dict(
            deepcopy(project),
            profile,
            packages,
            selectors,
            cli_vars,
        )

    args = Obj()
    args.vars = cli_vars
    args.profile_dir = '/dev/null'
    return RuntimeConfig.from_parts(
        project=project,
        profile=profile,
        args=args
    )


def inject_plugin(plugin):
    from dbt.adapters.factory import FACTORY
    key = plugin.adapter.type()
    FACTORY.plugins[key] = plugin


def inject_plugin_for(config):
    # from dbt.adapters.postgres import Plugin, PostgresAdapter
    from dbt.adapters.factory import FACTORY
    FACTORY.load_plugin(config.credentials.type)
    adapter = FACTORY.get_adapter(config)
    return adapter


def inject_adapter(value, plugin):
    """Inject the given adapter into the adapter factory, so your hand-crafted
    artisanal adapter will be available from get_adapter() as if dbt loaded it.
    """
    inject_plugin(plugin)
    from dbt.adapters.factory import FACTORY
    key = value.type()
    FACTORY.adapters[key] = value


def clear_plugin(plugin):
    from dbt.adapters.factory import FACTORY
    key = plugin.adapter.type()
    FACTORY.plugins.pop(key, None)
    FACTORY.adapters.pop(key, None)


class ContractTestCase(TestCase):
    ContractType = None

    def setUp(self):
        self.maxDiff = None
        super().setUp()

    def assert_to_dict(self, obj, dct):
        self.assertEqual(obj.to_dict(omit_none=True), dct)

    def assert_from_dict(self, obj, dct, cls=None):
        if cls is None:
            cls = self.ContractType
        cls.validate(dct)
        self.assertEqual(cls.from_dict(dct),  obj)

    def assert_symmetric(self, obj, dct, cls=None):
        self.assert_to_dict(obj, dct)
        self.assert_from_dict(obj, dct, cls)

    def assert_fails_validation(self, dct, cls=None):
        if cls is None:
            cls = self.ContractType

        with self.assertRaises(ValidationError):
            cls.validate(dct)
            cls.from_dict(dct)


def compare_dicts(dict1, dict2):
    first_set = set(dict1.keys())
    second_set = set(dict2.keys())
    print(f"--- Difference between first and second keys: {first_set.difference(second_set)}")
    print(f"--- Difference between second and first keys: {second_set.difference(first_set)}")
    common_keys = set(first_set).intersection(set(second_set))
    found_differences = False
    for key in common_keys:
        if dict1[key] != dict2[key]:
            print(f"--- --- first dict: {key}: {str(dict1[key])}")
            print(f"--- --- second dict: {key}: {str(dict2[key])}")
            found_differences = True
    if found_differences:
        print("--- Found differences in dictionaries")
    else:
        print("--- Found no differences in dictionaries")


def assert_to_dict(obj, dct):
    assert obj.to_dict(omit_none=True) == dct


def assert_from_dict(obj, dct, cls=None):
    if cls is None:
        cls = obj.__class__
    cls.validate(dct)
    assert cls.from_dict(dct) == obj


def assert_symmetric(obj, dct, cls=None):
    assert_to_dict(obj, dct)
    assert_from_dict(obj, dct, cls)


def assert_fails_validation(dct, cls):
    with pytest.raises(ValidationError):
        cls.validate(dct)
        cls.from_dict(dct)


def generate_name_macros(package):
    from dbt.contracts.graph.parsed import ParsedMacro
    from dbt.node_types import NodeType
    name_sql = {}
    for component in ('database', 'schema', 'alias'):
        if component == 'alias':
            source = 'node.name'
        else:
            source = f'target.{component}'
        name = f'generate_{component}_name'
        sql = f'{{% macro {name}(value, node) %}} {{% if value %}} {{{{ value }}}} {{% else %}} ' \
              f'{{{{ {source} }}}} {{% endif %}} {{% endmacro %}}'
        name_sql[name] = sql

    for name, sql in name_sql.items():
        pm = ParsedMacro(
            name=name,
            resource_type=NodeType.Macro,
            unique_id=f'macro.{package}.{name}',
            package_name=package,
            original_file_path=normalize('macros/macro.sql'),
            root_path='./dbt_modules/root',
            path=normalize('macros/macro.sql'),
            macro_sql=sql,
        )
        yield pm


class TestAdapterConversions(TestCase):
    def _get_tester_for(self, column_type):
        from dbt.clients import agate_helper
        if column_type is agate.TimeDelta:  # dbt never makes this!
            return agate.TimeDelta()

        for instance in agate_helper.DEFAULT_TYPE_TESTER._possible_types:
            if type(instance) is column_type:
                return instance

        raise ValueError(f'no tester for {column_type}')

    def _make_table_of(self, rows, column_types):
        column_names = list(string.ascii_letters[:len(rows[0])])
        if isinstance(column_types, type):
            column_types = [self._get_tester_for(column_types) for _ in column_names]
        else:
            column_types = [self._get_tester_for(typ) for typ in column_types]
        table = agate.Table(rows, column_names=column_names, column_types=column_types)
        return table


def MockMacro(package, name='my_macro', **kwargs):
    from dbt.contracts.graph.parsed import ParsedMacro
    from dbt.node_types import NodeType

    mock_kwargs = dict(
        resource_type=NodeType.Macro,
        package_name=package,
        unique_id=f'macro.{package}.{name}',
        original_file_path='/dev/null',
    )

    mock_kwargs.update(kwargs)

    macro = mock.MagicMock(
        spec=ParsedMacro,
        **mock_kwargs
    )
    macro.name = name
    return macro


def MockMaterialization(package, name='my_materialization', adapter_type=None, **kwargs):
    if adapter_type is None:
        adapter_type = 'default'
    kwargs['adapter_type'] = adapter_type
    return MockMacro(package, f'materialization_{name}_{adapter_type}', **kwargs)


def MockGenerateMacro(package, component='some_component', **kwargs):
    name = f'generate_{component}_name'
    return MockMacro(package, name=name, **kwargs)


def MockSource(package, source_name, name, **kwargs):
    from dbt.node_types import NodeType
    from dbt.contracts.graph.parsed import ParsedSourceDefinition
    src = mock.MagicMock(
        __class__=ParsedSourceDefinition,
        resource_type=NodeType.Source,
        source_name=source_name,
        package_name=package,
        unique_id=f'source.{package}.{source_name}.{name}',
        search_name=f'{source_name}.{name}',
        **kwargs
    )
    src.name = name
    return src


def MockNode(package, name, resource_type=None, **kwargs):
    from dbt.node_types import NodeType
    from dbt.contracts.graph.parsed import ParsedModelNode, ParsedSeedNode
    if resource_type is None:
        resource_type = NodeType.Model
    if resource_type == NodeType.Model:
        cls = ParsedModelNode
    elif resource_type == NodeType.Seed:
        cls = ParsedSeedNode
    else:
        raise ValueError(f'I do not know how to handle {resource_type}')
    node = mock.MagicMock(
        __class__=cls,
        resource_type=resource_type,
        package_name=package,
        unique_id=f'{str(resource_type)}.{package}.{name}',
        search_name=name,
        **kwargs
    )
    node.name = name
    return node


def MockDocumentation(package, name, **kwargs):
    from dbt.node_types import NodeType
    from dbt.contracts.graph.parsed import ParsedDocumentation
    doc = mock.MagicMock(
        __class__=ParsedDocumentation,
        resource_type=NodeType.Documentation,
        package_name=package,
        search_name=name,
        unique_id=f'{package}.{name}',
        **kwargs
    )
    doc.name = name
    return doc


def load_internal_manifest_macros(config, macro_hook=lambda m: None):
    from dbt.parser.manifest import ManifestLoader
    return ManifestLoader.load_macros(config, macro_hook)


def dict_replace(dct, **kwargs):
    dct = dct.copy()
    dct.update(kwargs)
    return dct


def replace_config(n, **kwargs):
    return n.replace(
        config=n.config.replace(**kwargs),
        unrendered_config=dict_replace(n.unrendered_config, **kwargs),
    )
