// SPDX-License-Identifier: Apache-2.0

import { DatasetVersions } from '../../types/api'
import {
  FETCH_DATASET_VERSIONS,
  FETCH_DATASET_VERSIONS_SUCCESS,
  RESET_DATASET_VERSIONS
} from '../actionCreators/actionTypes'
import { fetchDatasetVersionsSuccess } from '../actionCreators'

export type IDatasetVersionsState = { isLoading: boolean; result: DatasetVersions; init: boolean }

export const initialState: IDatasetVersionsState = {
  isLoading: false,
  init: false,
  result: { versions: [] }
}

type IDatasetVersionAction = ReturnType<typeof fetchDatasetVersionsSuccess>

export default (
  state: IDatasetVersionsState = initialState,
  action: IDatasetVersionAction
): IDatasetVersionsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_DATASET_VERSIONS:
      return { ...state, isLoading: true }
    case FETCH_DATASET_VERSIONS_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload }
    case RESET_DATASET_VERSIONS:
      return initialState
    default:
      return state
  }
}
