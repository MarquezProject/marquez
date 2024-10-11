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
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone
  const url = `${API_URL}/stats/lineage-events?period=${payload.unit.toUpperCase()}&timezone=${timezone}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchLineageMetrics')
}
