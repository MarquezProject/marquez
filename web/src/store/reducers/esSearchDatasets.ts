import { EsSearchResultDatasets } from '../../types/api'
import {
  FETCH_ES_SEARCH_DATASETS,
  FETCH_ES_SEARCH_DATASETS_SUCCESS,
} from '../actionCreators/actionTypes'
import { fetchEsSearchDatasets, fetchEsSearchDatasetsSuccess } from '../actionCreators'

export type IEsSearchDatasetsState = {
  isLoading: boolean
  data: EsSearchResultDatasets
  init: boolean
}

export const initialState: IEsSearchDatasetsState = {
  isLoading: false,
  data: { hits: [], highlights: [] },
  init: false,
}

type IDatasetsAction = ReturnType<typeof fetchEsSearchDatasetsSuccess> &
  ReturnType<typeof fetchEsSearchDatasets>

export default (state = initialState, action: IDatasetsAction): IEsSearchDatasetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_ES_SEARCH_DATASETS:
      return { ...state, isLoading: true }
    case FETCH_ES_SEARCH_DATASETS_SUCCESS: {
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
