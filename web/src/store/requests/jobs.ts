// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Jobs } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getJobs = async (namespace: string, limit = 25, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/jobs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return { totalCount: r.totalCount, jobs: r.jobs.map((j) => ({ ...j, namespace: namespace })) }
  })
}

export const deleteJob = async (jobName: string, namespace: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${jobName}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteJob')
}

export const getRuns = async (
  jobName: string,
  namespace: string,
  limit: number,
  offset: number
) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${encodeURIComponent(
    jobName
  )}/runs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchRuns')
}

export const getJob = async (namespace: string, job: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${job}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchJob')
}

export const deleteJobTag = async (namespace: string, jobName: string, tag: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${jobName}/tags/${tag}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteJobTag')
}

export const addJobTag = async (namespace: string, jobName: string, tag: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${jobName}/tags/${tag}`
  return genericFetchWrapper(url, { method: 'POST' }, 'addJobTag')
}
