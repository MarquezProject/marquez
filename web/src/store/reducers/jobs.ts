// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  ADD_JOB_TAG,
  ADD_JOB_TAG_SUCCESS,
  DELETE_JOB,
  DELETE_JOB_SUCCESS,
  DELETE_JOB_TAG,
  DELETE_JOB_TAG_SUCCESS,
  FETCH_JOBS,
  FETCH_JOBS_SUCCESS,
  FETCH_JOB_TAGS,
  FETCH_JOB_TAGS_SUCCESS,
  RESET_JOBS,
} from '../actionCreators/actionTypes'
import { IJob } from '../../types'
import {
  addJobTag,
  deleteJob,
  deleteJobTag,
  fetchJobTagsSuccess,
  fetchJobsSuccess,
} from '../actionCreators'

export type IJobsState = {
  isLoading: boolean
  result: IJob[]
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

export type IJobsAction = ReturnType<typeof fetchJobsSuccess> &
  ReturnType<typeof deleteJob> &
  ReturnType<typeof fetchJobTagsSuccess> &
  ReturnType<typeof deleteJobTag> &
  ReturnType<typeof addJobTag>

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
    case FETCH_JOB_TAGS:
      return { ...state, isLoading: true }
    case FETCH_JOB_TAGS_SUCCESS:
      return {
        ...state,
        isLoading: false,
        jobTags: payload.jobTags,
      }
    case ADD_JOB_TAG:
      return {
        ...state,
      }
    case ADD_JOB_TAG_SUCCESS:
      return {
        ...state,
      }
    case DELETE_JOB_TAG:
      return {
        ...state,
      }
    case DELETE_JOB_TAG_SUCCESS:
      return {
        ...state,
      }
    default:
      return state
  }
}
