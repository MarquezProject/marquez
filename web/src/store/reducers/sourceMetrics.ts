import { FETCH_SOURCE_METRICS, FETCH_SOURCE_METRICS_SUCCESS } from '../actionCreators/actionTypes'
import { IntervalMetric } from '../requests/intervalMetrics'
import { fetchSourceMetrics, fetchSourceMetricsSuccess } from '../actionCreators'

export type ISourceMetricsState = {
  isLoading: boolean
  data: IntervalMetric[]
  init: boolean
}

export const initialState: ISourceMetricsState = {
  isLoading: false,
  data: [],
  init: false,
}

type ISourceMetricAction = ReturnType<typeof fetchSourceMetricsSuccess> &
  ReturnType<typeof fetchSourceMetrics>

export default (state = initialState, action: ISourceMetricAction): ISourceMetricsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_SOURCE_METRICS:
      return { ...state, isLoading: true }
    case FETCH_SOURCE_METRICS_SUCCESS: {
      return {
        ...state,
        isLoading: false,
        init: true,
        data: payload,
      }
    }
    default:
      return state
  }
}
