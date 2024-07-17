// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  FETCH_LATEST_RUNS,
  FETCH_LATEST_RUNS_SUCCESS,
  FETCH_RUNS,
  FETCH_RUNS_SUCCESS,
  RESET_RUNS,
} from '../actionCreators/actionTypes'
import { Run } from '../../types/api'
import { fetchRunsSuccess } from '../actionCreators'

export type IRunsState = {
  isLoading: boolean
  result: Run[]
  init: boolean
  totalCount: number
  latestRuns: Run[]
  isLatestRunsLoading: boolean
}

export const initialState: IRunsState = {
  isLoading: false,
  result: [],
  init: false,
  totalCount: 0,
  latestRuns: [],
  isLatestRunsLoading: false,
}

type IRunsAction = ReturnType<typeof fetchRunsSuccess>

export default (state = initialState, action: IRunsAction): IRunsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_RUNS:
      return { ...state, isLoading: true }
    case FETCH_RUNS_SUCCESS:
      return {
        ...state,
        isLoading: false,
        init: true,
        result: payload.runs,
        totalCount: payload.totalCount,
      }
    case FETCH_LATEST_RUNS:
      return { ...state, isLatestRunsLoading: true }
    case FETCH_LATEST_RUNS_SUCCESS:
      return {
        ...state,
        isLatestRunsLoading: false,
        latestRuns: payload.runs,
      }
    case RESET_RUNS:
      return initialState
    default:
      return state
  }
}
