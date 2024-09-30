import { FETCH_JOB_METRICS, FETCH_JOB_METRICS_SUCCESS } from '../actionCreators/actionTypes'
import { IntervalMetric } from '../requests/intervalMetrics'
import { fetchJobMetrics, fetchJobMetricsSuccess } from '../actionCreators'

export type IJobMetricsState = {
  isLoading: boolean
  data: IntervalMetric[]
  init: boolean
}

export const initialState: IJobMetricsState = {
  isLoading: false,
  data: [],
  init: false,
}

type IJobMetricAction = ReturnType<typeof fetchJobMetricsSuccess> &
  ReturnType<typeof fetchJobMetrics>

export default (state = initialState, action: IJobMetricAction): IJobMetricsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_JOB_METRICS:
      return { ...state, isLoading: true }
    case FETCH_JOB_METRICS_SUCCESS: {
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
