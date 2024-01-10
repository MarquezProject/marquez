// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  ADD_DATASET_TAG,
  ADD_DATASET_TAG_SUCCESS,
  DELETE_DATASET,
  DELETE_DATASET_SUCCESS,
  DELETE_DATASET_TAG,
  DELETE_DATASET_TAG_SUCCESS,
  FETCH_DATASETS,
  FETCH_DATASETS_SUCCESS,
  RESET_DATASETS,
} from '../actionCreators/actionTypes'
import { Dataset } from '../../types/api'
import {
  addDatasetTag,
  deleteDataset,
  deleteDatasetTag,
  fetchDatasetsSuccess,
} from '../actionCreators'

export type IDatasetsState = {
  isLoading: boolean
  result: Dataset[]
  totalCount: number
  init: boolean
  deletedDatasetName: string
  refreshTags: boolean
}

export const initialState: IDatasetsState = {
  isLoading: false,
  init: false,
  result: [],
  totalCount: 0,
  deletedDatasetName: '',
  refreshTags: false,
}

export type IDatasetsAction = ReturnType<typeof fetchDatasetsSuccess> &
  ReturnType<typeof deleteDataset> &
  ReturnType<typeof deleteDatasetTag> &
  ReturnType<typeof addDatasetTag>

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
    case DELETE_DATASET_TAG:
      return { ...state, refreshTags: false }
    case DELETE_DATASET_TAG_SUCCESS:
      return { ...state, refreshTags: true }
    case ADD_DATASET_TAG:
      return { ...state, refreshTags: false }
    case ADD_DATASET_TAG_SUCCESS:
      return { ...state, refreshTags: true }
    default:
      return state
  }
}
