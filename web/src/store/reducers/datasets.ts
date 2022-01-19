// SPDX-License-Identifier: Apache-2.0

import { Dataset } from '../../types/api'
import {
  FETCH_DATASETS,
  FETCH_DATASETS_SUCCESS,
  RESET_DATASETS
} from '../actionCreators/actionTypes'
import { fetchDatasetsSuccess } from '../actionCreators'

export type IDatasetsState = { isLoading: boolean; result: Dataset[]; init: boolean }

export const initialState: IDatasetsState = { isLoading: false, init: false, result: [] }

type IDatasetsAction = ReturnType<typeof fetchDatasetsSuccess>

export default (state: IDatasetsState = initialState, action: IDatasetsAction): IDatasetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_DATASETS:
      return { ...state, isLoading: true }
    case FETCH_DATASETS_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload.datasets }
    case RESET_DATASETS:
      return initialState
    default:
      return state
  }
}
