import { genericFetchWrapper } from '.'
import { Namespaces } from '../types/api'

export const fetchNamespaces = async () => {
  const url = `${__API_URL__}/namespaces`
  return genericFetchWrapper<Namespaces>(url, { method: 'GET' }, 'fetchNamespaces')
}
