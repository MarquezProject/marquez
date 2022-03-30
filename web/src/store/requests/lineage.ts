// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { JobOrDataset } from '../../components/lineage/types'
import { generateNodeId } from '../../helpers/nodes'
import { genericFetchWrapper } from './index'

export const getLineage = async (nodeType: JobOrDataset, namespace: string, name: string) => {
  const url = `${API_URL}/lineage/?nodeId=${generateNodeId(nodeType, namespace, name)}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchLineage')
}
