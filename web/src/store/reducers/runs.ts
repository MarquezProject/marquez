// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { 
  FETCH_RUNS, 
  FETCH_RUNS_SUCCESS, 
  RESET_RUNS 
} from '../actionCreators/actionTypes'
import { Run } from '../../types/api'
import { fetchRunsSuccess } from '../actionCreators'

export type IRunsState = { 
  isLoading: boolean
  result: Run[]
  totalCount: number
  init: boolean 
}

export const initialState: IRunsState = { 
  isLoading: false, 
  result: [], 
  totalCount: 0, 
  init: false 
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
    case RESET_RUNS:
      return initialState
    default:
      return state
  }
}
