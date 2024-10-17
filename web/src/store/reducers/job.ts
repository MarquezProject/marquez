// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { FETCH_JOB, FETCH_JOB_SUCCESS } from '../actionCreators/actionTypes'
import { Job } from '../../types/api'
import { Nullable } from '../../types/util/Nullable'
import { fetchJobSuccess, fetchJobsSuccess } from '../actionCreators'

export type IJobState = {
  isLoading: boolean
  result: Nullable<Job>
}

export const initialState: IJobState = {
  isLoading: false,
  result: null,
}

export type IJobsAction = ReturnType<typeof fetchJobsSuccess> & ReturnType<typeof fetchJobSuccess>

export default (state = initialState, action: IJobsAction): IJobState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOB:
      return { ...state, isLoading: true }
    case FETCH_JOB_SUCCESS:
      return {
        ...state,
        isLoading: false,
        result: payload.job,
      }
    default:
      return state
  }
}
