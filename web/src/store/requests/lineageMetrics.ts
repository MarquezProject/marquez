import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export interface LineageMetric {
  startInterval: string
  endInterval: string
  fail: number
  start: number
  complete: number
  abort: number
}

export const getLineageMetrics = async (payload: { unit: 'day' | 'week' }) => {
  const url = `${API_URL}/ops/lineage-metrics/${payload.unit}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchLineageMetrics')
}
