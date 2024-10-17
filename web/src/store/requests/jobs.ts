// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Jobs, RunState } from '../../types/api'
import { Nullable } from '../../types/util/Nullable'
import { genericFetchWrapper } from './index'

export const getJobs = async (
  namespace: Nullable<string>,
  limit = 25,
  offset = 0,
  lastRunStates?: RunState
) => {
  let url = `${API_URL}/jobs?limit=${limit}&offset=${offset}`
  if (namespace) {
    const encodedNamespace = encodeURIComponent(namespace)
    url = `${API_URL}/namespaces/${encodedNamespace}/jobs?limit=${limit}&offset=${offset}`
  }
  if (lastRunStates) {
    url += `&lastRunStates=${lastRunStates}`
  }
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return { totalCount: r.totalCount, jobs: r.jobs }
  })
}

export const deleteJob = async (namespace: string, jobName: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedJob = encodeURIComponent(jobName)
  const url = `${API_URL}/namespaces/${encodedNamespace}/jobs/${encodedJob}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteJob')
}

export const getRuns = async (
  jobName: string,
  namespace: string,
  limit: number,
  offset: number
) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedJob = encodeURIComponent(jobName)
  const url = `${API_URL}/namespaces/${encodedNamespace}/jobs/${encodedJob}/runs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchRuns')
}

export const getJob = async (namespace: string, jobName: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedJob = encodeURIComponent(jobName)
  const url = `${API_URL}/namespaces/${encodedNamespace}/jobs/${encodedJob}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchJob')
}

export const deleteJobTag = async (namespace: string, jobName: string, tag: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedJob = encodeURIComponent(jobName)
  const encodedTag = encodeURIComponent(tag)
  const url = `${API_URL}/namespaces/${encodedNamespace}/jobs/${encodedJob}/tags/${encodedTag}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteJobTag')
}

export const addJobTag = async (namespace: string, jobName: string, tag: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedJob = encodeURIComponent(jobName)
  const encodedTag = encodeURIComponent(tag)
  const url = `${API_URL}/namespaces/${encodedNamespace}/jobs/${encodedJob}/tags/${encodedTag}`
  return genericFetchWrapper(url, { method: 'POST' }, 'addJobTag')
}

export const getJobsByState = async (runState: RunState, limit: number, offset: number) => {
  const url = `${API_URL}/jobs?runState=${runState}&limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'POST' }, 'fetchJobsByState')
}
