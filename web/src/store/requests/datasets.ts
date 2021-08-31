import { API_URL } from '../../globals'
import { Datasets, Namespace } from '../../types/api'
import { genericFetchWrapper } from './index'

export const fetchDatasets = async (namespace: Namespace) => {
  const { name } = namespace

  const url = `${API_URL}/namespaces/${encodeURIComponent(name)}/datasets?limit=700`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace.name }))
  })
}
