import { Datasets, Namespace } from '../types/api'
import { genericFetchWrapper } from '.'

export const fetchDatasets = async (namespace: Namespace) => {
  const { name } = namespace
  // eslint-disable-next-line no-undef
  const url = `${__API_URL__}/namespaces/${encodeURIComponent(name)}/datasets?limit=700`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace.name }))
  })
}
