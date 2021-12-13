#!/usr/bin/env python
#
# SPDX-License-Identifier: Apache-2.0
#
# -*- coding: utf-8 -*-

from setuptools import find_packages, setup

with open("README.md") as readme_file:
    readme = readme_file.read()

requirements = [
    "openlineage-airflow==0.3.1",
]

setup(
    name="marquez-airflow",
    version="0.20.0",
    description="Marquez integration with Airflow",
    long_description=readme,
    long_description_content_type="text/markdown",
    author="Marquez Project",
    packages=find_packages(),
    include_package_data=True,
    install_requires=requirements,
    python_requires=">=3.6",
    zip_safe=False,
    keywords="marquez",
)
