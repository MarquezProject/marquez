// SPDX-License-Identifier: Apache-2.0

import { FETCH_JOBS, FETCH_JOBS_SUCCESS, RESET_JOBS } from '../actionCreators/actionTypes'
import { IJob } from '../../types'
import { fetchJobsSuccess } from '../actionCreators'

export type IJobsState = { isLoading: boolean; result: IJob[]; init: boolean }

export const initialState: IJobsState = { isLoading: false, result: [], init: false }

type IJobsAction = ReturnType<typeof fetchJobsSuccess>

export default (state = initialState, action: IJobsAction): IJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOBS:
      return { ...state, isLoading: true }
    case FETCH_JOBS_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload.jobs }
    case RESET_JOBS:
      return initialState
    default:
      return state
  }
}
