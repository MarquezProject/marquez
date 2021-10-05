import { API_URL } from '../../globals'
import { Job, Jobs, Run } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getJobs = async (namespace: string, limit = 2000, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/jobs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper<Job[]>(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return r.jobs.map(j => ({ ...j, namespace: namespace }))
  })
}

export const getRuns = async (jobName: string, namespace: string, limit = 100, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${encodeURIComponent(
    jobName
  )}/runs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper<Run[]>(url, { method: 'GET' }, 'fetchRuns')
}
