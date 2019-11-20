import { genericFetchWrapper } from '.'
import { INamespaceAPI, IDatasetsAPI } from '../types/api'

export const fetchDatasets = async (namespace: INamespaceAPI) => {
  const { name } = namespace
  const url = `${__API_URL__}/namespaces/${name}/datasets?limit=700`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: IDatasetsAPI) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace.name }))
  })
}
