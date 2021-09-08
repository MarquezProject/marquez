import { API_URL } from '../../globals'
import { Datasets } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getDatasets = async (namespace: string, limit = 2000, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/datasets?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace }))
  })
}
