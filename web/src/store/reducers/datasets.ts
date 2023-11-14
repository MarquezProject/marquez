// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  DELETE_DATASET,
  DELETE_DATASET_SUCCESS,
  FETCH_DATASETS,
  FETCH_DATASETS_SUCCESS,
  RESET_DATASETS,
} from '../actionCreators/actionTypes'
import { Dataset } from '../../types/api'
import { deleteDataset, fetchDatasetsSuccess } from '../actionCreators'

export type IDatasetsState = {
  isLoading: boolean
  result: Dataset[]
  totalCount: number
  init: boolean
  deletedDatasetName: string
}

export const initialState: IDatasetsState = {
  isLoading: false,
  init: false,
  result: [],
  totalCount: 0,
  deletedDatasetName: '',
}

export type IDatasetsAction = ReturnType<typeof fetchDatasetsSuccess> &
  ReturnType<typeof deleteDataset>

export default (state: IDatasetsState = initialState, action: IDatasetsAction): IDatasetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_DATASETS:
      return { ...state, isLoading: true }
    case FETCH_DATASETS_SUCCESS:
      return {
        ...state,
        isLoading: false,
        init: true,
        result: payload.datasets,
        totalCount: payload.totalCount,
      }
    case RESET_DATASETS:
      return initialState
    case DELETE_DATASET:
      return { ...state, result: state.result.filter((e) => e.name !== payload.datasetName) }
    case DELETE_DATASET_SUCCESS:
      return { ...state, deletedDatasetName: payload.datasetName }
    default:
      return state
  }
}
