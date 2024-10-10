import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export interface IntervalMetric {
  startInterval: string
  endInterval: string
  count: number
}

export const getIntervalMetrics = async (payload: {
  asset: 'jobs' | 'datasets' | 'sources'
  unit: 'day' | 'week'
}) => {
  const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone
  const url = `${API_URL}/stats/${
    payload.asset
  }?period=${payload.unit.toUpperCase()}&timezone=${timezone}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchIntervalMetrics')
}
