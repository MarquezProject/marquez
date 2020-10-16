import { Datasets, Namespace } from '../types/api'
import { genericFetchWrapper } from '.'

export const fetchDatasets = async (namespace: Namespace) => {
  const { name } = namespace
  const url = `${__API_URL__}/namespaces/${name}/datasets?limit=700`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace.name }))
  })
}
