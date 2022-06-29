# Copyright 2018-2022 contributors to the Marquez project
# SPDX-License-Identifier: Apache-2.0

from enum import Enum


class DatasetId:
    def __init__(self, namespace: str, name: str):
        self.namespace = namespace
        self.name = name


class JobId:
    def __init__(self, namespace: str, name: str):
        self.namespace = namespace
        self.name = name


class DatasetType(Enum):
    DB_TABLE = "DB_TABLE"
    STREAM = "STREAM"


class JobType(Enum):
    BATCH = "BATCH"
    STREAM = "STREAM"
    SERVICE = "SERVICE"


class RunState(Enum):
    NEW = 'NEW'
    RUNNING = 'RUNNING'
    COMPLETED = 'COMPLETED'
    FAILED = 'FAILED'
    ABORTED = 'ABORTED'
