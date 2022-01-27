// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Namespaces } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getNamespaces = async () => {
  const url = `${API_URL}/namespaces`
  return genericFetchWrapper<Namespaces>(url, { method: 'GET' }, 'fetchNamespaces')
}
