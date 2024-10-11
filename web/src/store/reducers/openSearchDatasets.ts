import {
  FETCH_OPEN_SEARCH_DATASETS,
  FETCH_OPEN_SEARCH_DATASETS_SUCCESS,
} from '../actionCreators/actionTypes'
import { OpenSearchResultDatasets } from '../../types/api'
import { fetchOpenSearchDatasets, fetchOpenSearchDatasetsSuccess } from '../actionCreators'

export type IOpenSearchDatasetsState = {
  isLoading: boolean
  data: OpenSearchResultDatasets
  init: boolean
}

export const initialState: IOpenSearchDatasetsState = {
  isLoading: false,
  data: { hits: [], highlights: [] },
  init: false,
}

type IDatasetsAction = ReturnType<typeof fetchOpenSearchDatasetsSuccess> &
  ReturnType<typeof fetchOpenSearchDatasets>

export default (state = initialState, action: IDatasetsAction): IOpenSearchDatasetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_OPEN_SEARCH_DATASETS:
      return { ...state, isLoading: true }
    case FETCH_OPEN_SEARCH_DATASETS_SUCCESS: {
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
