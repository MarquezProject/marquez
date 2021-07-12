import { Job, Jobs, Namespace, Run } from '../types/api'
import { genericFetchWrapper } from '.'

export const fetchJobs = async (namespace: Namespace) => {
  const { name } = namespace
  // eslint-disable-next-line no-undef
  const url = `${__API_URL__}/namespaces/${encodeURIComponent(name)}/jobs?limit=700`
  return genericFetchWrapper<Job[]>(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return r.jobs.map(j => ({ ...j, namespace: namespace.name }))
  })
}

export const fetchLatestJobRuns = async (jobName: string, namespace: string) => {
  // eslint-disable-next-line no-undef
  const url = `${__API_URL__}/namespaces/${encodeURIComponent(namespace)}/jobs/${encodeURIComponent(
    jobName
  )}/runs?limit=10`
  return genericFetchWrapper<Run[]>(url, { method: 'GET' }, 'fetchLatestJobRuns')
}
