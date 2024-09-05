import { FETCH_LINEAGE_METRICS, FETCH_LINEAGE_METRICS_SUCCESS } from '../actionCreators/actionTypes'
import { LineageMetric } from '../requests/lineageMetrics'
import { fetchLineageMetrics, fetchLineageMetricsSuccess } from '../actionCreators'

export type ILineageMetricsState = {
  isLoading: boolean
  data: LineageMetric[]
  init: boolean
}

export const initialState: ILineageMetricsState = {
  isLoading: false,
  data: [],
  init: false,
}

type IDatasetsAction = ReturnType<typeof fetchLineageMetricsSuccess> &
  ReturnType<typeof fetchLineageMetrics>

export default (state = initialState, action: IDatasetsAction): ILineageMetricsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_LINEAGE_METRICS:
      return { ...state, isLoading: true }
    case FETCH_LINEAGE_METRICS_SUCCESS: {
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
