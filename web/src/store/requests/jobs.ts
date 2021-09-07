import { API_URL } from '../../globals'
import { Job, Jobs, Namespace, Run } from '../../types/api'
import { genericFetchWrapper } from './index'

export const fetchJobs = async (namespace: Namespace, limit = 20, offset = 0) => {
  const { name } = namespace
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    name
  )}/jobs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper<Job[]>(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return r.jobs.map(j => ({ ...j, namespace: namespace.name }))
  })
}

export const fetchLatestJobRuns = async (
  jobName: string,
  namespace: string,
  limit = 20,
  offset = 0
) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/jobs/${encodeURIComponent(
    jobName
  )}/runs?limit=${limit}&offset=${offset}`
  return genericFetchWrapper<Run[]>(url, { method: 'GET' }, 'fetchLatestJobRuns')
}
