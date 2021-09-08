import { API_URL } from '../../globals'
import { JobOrDataset } from '../../components/lineage/types'
import { Run } from '../../types/api'
import { encodeNode } from '../../helpers/nodes'
import { genericFetchWrapper } from './index'

export const getLineage = async (nodeType: JobOrDataset, namespace: string, name: string) => {
  const url = `${API_URL}/lineage/${encodeNode(nodeType, namespace, name)}`
  return genericFetchWrapper<Run[]>(url, { method: 'GET' }, 'fetchLineage')
}
