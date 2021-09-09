import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export const getSearch = async (q: string, filter = 'ALL', sort = 'RECENT', limit = 100) => {
  const url = `${API_URL}/search/?q=${q}&filter=${filter}&sort=${sort}&limit=${limit}`
  return genericFetchWrapper<any[]>(url, { method: 'GET' }, 'fetchSearch')
}
