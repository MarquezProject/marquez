import { Namespaces } from '../types/api'
import { genericFetchWrapper } from '.'

export const fetchNamespaces = async () => {
  // eslint-disable-next-line no-undef
  const url = `${__API_URL__}/namespaces`
  return genericFetchWrapper<Namespaces>(url, { method: 'GET' }, 'fetchNamespaces')
}
