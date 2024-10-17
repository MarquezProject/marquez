// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  DELETE_JOB,
  DELETE_JOB_SUCCESS,
  FETCH_JOBS,
  FETCH_JOBS_SUCCESS,
  RESET_JOBS,
} from '../actionCreators/actionTypes'
import { Job } from '../../types/api'
import { deleteJob, fetchJobsSuccess } from '../actionCreators'

export type IJobsState = {
  isLoading: boolean
  result: Job[]
  totalCount: number
  init: boolean
  deletedJobName: string
  jobTags: string[]
}

export const initialState: IJobsState = {
  isLoading: false,
  result: [],
  totalCount: 0,
  init: false,
  deletedJobName: '',
  jobTags: [],
}

export type IJobsAction = ReturnType<typeof fetchJobsSuccess> & ReturnType<typeof deleteJob>

export default (state = initialState, action: IJobsAction): IJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOBS:
      return { ...state, isLoading: true }
    case FETCH_JOBS_SUCCESS:
      return {
        ...state,
        isLoading: false,
        init: true,
        result: payload.jobs,
        totalCount: payload.totalCount,
      }
    case RESET_JOBS:
      return initialState
    case DELETE_JOB:
      return { ...state, result: state.result.filter((e) => e.name !== payload.jobName) }
    case DELETE_JOB_SUCCESS:
      return { ...state, deletedJobName: payload.jobName }
    default:
      return state
  }
}
