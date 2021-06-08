#!/usr/bin/env python
from setuptools import find_packages
from setuptools import setup

package_name = "dbt-openlineage-bigquery"
package_version = "0.14.2"
description = """The openlineage bigquery adapter plugin for dbt (data build tool)"""

extras_require = {
    "tests": [
        "pytest",
        "pytest-cov",
        "mock",
        "flake8",
    ],
}
extras_require["dev"] = set(sum(extras_require.values(), []))


setup(
    name=package_name,
    version=package_version,
    description=description,
    long_description=description,
    author='Marquez Project',
    packages=find_packages(),
    package_data={
        'dbt': [
            'include/openlineage-bigquery/macros/*.sql',
            'include/openlineage-bigquery/dbt_project.yml',
        ]
    },
    install_requires=[
        "dbt-core>=0.20.0b1",
        "sqlparse>=0.4.1",
        "openlineage-python==0.0.1rc3"
    ],
    extras_require=extras_require,
    python_requires=">=3.6",
)
