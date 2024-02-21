// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Dataset } from '../../types/api'
import { FETCH_DATASET, FETCH_DATASET_SUCCESS, RESET_DATASET } from '../actionCreators/actionTypes'
import { Nullable } from '../../types/util/Nullable'
import { fetchDatasetSuccess } from '../actionCreators'

export type IDatasetState = { isLoading: boolean; result: Nullable<Dataset>; init: boolean }

export const initialState: IDatasetState = { isLoading: false, init: false, result: null }

type IDatasetAction = ReturnType<typeof fetchDatasetSuccess>

export default (state: IDatasetState = initialState, action: IDatasetAction): IDatasetState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_DATASET:
      return { ...state, isLoading: true }
    case FETCH_DATASET_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload.dataset }
    case RESET_DATASET:
      return initialState
    default:
      return state
  }
}
