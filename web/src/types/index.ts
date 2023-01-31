// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Job, Run } from './api'

export type HttpMethod = 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'

export type APIError = any

export interface IJob extends Job {
  latestRuns?: Run[]
}

export type IFilterByDisplay = 'namespace' | 'sourceName'
