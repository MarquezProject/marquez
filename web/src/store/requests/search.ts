// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_BETA_URL, API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export const getSearch = async (q: string, filter = 'ALL', sort = 'NAME', limit = 100) => {
  let url = `${API_URL}/search?q=${q}&sort=${sort}&limit=${limit}`
  if (filter === 'JOB' || filter === 'DATASET') {
    url += `&filter=${filter}`
  }
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchSearch')
}

export const getOpenSearchJobs = async (q: string) => {
  const url = `${API_BETA_URL}/search/jobs?q=${q}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchOpenSearchJobs')
}

export const getOpenSearchDatasets = async (q: string) => {
  const url = `${API_BETA_URL}/search/datasets?q=${q}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchOpenSearchDatasets')
}
