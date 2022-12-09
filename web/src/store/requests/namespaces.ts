// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export const getNamespaces = async () => {
  const url = `${API_URL}/namespaces`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchNamespaces')
}
