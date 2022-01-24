// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Search } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getSearch = async (q: string, filter = 'ALL', sort = 'NAME', limit = 100) => {
  let url = `${API_URL}/search/?q=${q}&sort=${sort}&limit=${limit}`
  if (filter === 'JOB' || filter === 'DATASET') {
    url += `&filter=${filter}`
  }
  return genericFetchWrapper<Search>(url, { method: 'GET' }, 'fetchSearch')
}
