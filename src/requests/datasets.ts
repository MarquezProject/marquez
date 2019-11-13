import { genericFetchWrapper } from '.'
import { INamespaceAPI } from '../types/api'

export const fetchDatasets = async (namespace: INamespaceAPI) => {
  const { name } = namespace
  const url = `${__API_URL__}/namespaces/${name}/datasets?limit=700`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets')
}
