import { API_URL } from '../../globals'
import { ColumnLineageGraph } from '../../types/api'
import { JobOrDataset } from '../../types/lineage'

import { generateNodeId } from '../../helpers/nodes'
import { genericFetchWrapper } from './index'

export interface IColumnLineageState {
  columnLineage: ColumnLineageGraph
}

export const getColumnLineage = async (
  nodeType: JobOrDataset,
  namespace: string,
  name: string,
  depth: number
) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedName = encodeURIComponent(name)
  const nodeId = generateNodeId(nodeType, encodedNamespace, encodedName)
  const url = `${API_URL}/column-lineage?nodeId=${nodeId}&depth=${depth}&withDownstream=true`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchColumnLineage')
}
