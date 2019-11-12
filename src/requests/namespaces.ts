import { genericFetchWrapper } from '.'
import { INamespacesAPI } from '../types/api'

export const fetchNamespaces = async () => {
  const url = `${__API_URL__}/namespaces`
  return genericFetchWrapper<INamespacesAPI>(url, { method: 'GET' }, 'fetchNamespaces')
}
