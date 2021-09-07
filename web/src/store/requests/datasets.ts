import { API_URL } from '../../globals'
import { Datasets } from '../../types/api'
import { genericFetchWrapper } from './index'

export const fetchDatasets = async (namespace: string, limit = 20, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/datasets?limit=${limit}&offset=${offset}`
  console.log(url)
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace }))
  })
}
