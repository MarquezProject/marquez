// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Jobs } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getJobs = async (namespace: string, limit = 25, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/jobs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return r.jobs.map(j => ({ ...j, namespace: namespace }))
  })
}

export const getRuns = async (jobName: string, namespace: string, limit = 100, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${encodeURIComponent(
    jobName
  )}/runs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchRuns')
}
