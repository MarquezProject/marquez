import { genericFetchWrapper } from '.'
import { Job, Namespace, Jobs, Run } from '../types/api'

export const fetchJobs = async (namespace: Namespace) => {
  const { name } = namespace
  const url = `${__API_URL__}/namespaces/${name}/jobs?limit=700`
  return genericFetchWrapper<Job[]>(url, { method: 'GET' }, 'fetchJobs').then((r: Jobs) => {
    return r.jobs.map(j => ({ ...j, namespace: namespace.name }))
  })
}

export const fetchLatestJobRuns = async (jobName: string, namespace: string) => {
  const url = `${__API_URL__}/namespaces/${namespace}/jobs/${jobName}/runs?limit=10`
  return genericFetchWrapper<Run[]>(url, { method: 'GET' }, 'fetchLatestJobRuns')
}
