// SPDX-License-Identifier: Apache-2.0

import { FETCH_RUNS, FETCH_RUNS_SUCCESS, RESET_RUNS } from '../actionCreators/actionTypes'
import { Run } from '../../types/api'
import { fetchRunsSuccess } from '../actionCreators'

export type IRunsState = { isLoading: boolean; result: Run[]; init: boolean }

export const initialState: IRunsState = { isLoading: false, result: [], init: false }

type IRunsAction = ReturnType<typeof fetchRunsSuccess>

export default (state = initialState, action: IRunsAction): IRunsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_RUNS:
      return { ...state, isLoading: true }
    case FETCH_RUNS_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload.runs }
    case RESET_RUNS:
      return initialState
    default:
      return state
  }
}
