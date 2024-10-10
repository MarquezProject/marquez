import { FETCH_DATASET_METRICS, FETCH_DATASET_METRICS_SUCCESS } from '../actionCreators/actionTypes'
import { IntervalMetric } from '../requests/intervalMetrics'
import { fetchDatasetMetrics, fetchDatasetMetricsSuccess } from '../actionCreators'

export type IDatasetMetricsState = {
  isLoading: boolean
  data: IntervalMetric[]
  init: boolean
}

export const initialState: IDatasetMetricsState = {
  isLoading: false,
  data: [],
  init: false,
}

type IDatasetMetricAction = ReturnType<typeof fetchDatasetMetricsSuccess> &
  ReturnType<typeof fetchDatasetMetrics>

export default (state = initialState, action: IDatasetMetricAction): IDatasetMetricsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_DATASET_METRICS:
      return { ...state, isLoading: true }
    case FETCH_DATASET_METRICS_SUCCESS: {
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
