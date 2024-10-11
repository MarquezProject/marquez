// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  FETCH_OPEN_SEARCH_JOBS,
  FETCH_OPEN_SEARCH_JOBS_SUCCESS,
} from '../actionCreators/actionTypes'

import { OpenSearchResultJobs } from '../../types/api'
import { fetchOpenSearchJobs, fetchOpenSearchJobsSuccess } from '../actionCreators'

export type IOpenSearchJobsState = { isLoading: boolean; data: OpenSearchResultJobs; init: boolean }

export const initialState: IOpenSearchJobsState = {
  isLoading: false,
  data: { hits: [], highlights: [] },
  init: false,
}

type IJobsAction = ReturnType<typeof fetchOpenSearchJobsSuccess> &
  ReturnType<typeof fetchOpenSearchJobs>

export default (state = initialState, action: IJobsAction): IOpenSearchJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_OPEN_SEARCH_JOBS:
      return { ...state, isLoading: true }
    case FETCH_OPEN_SEARCH_JOBS_SUCCESS: {
      return {
        ...state,
        isLoading: false,
        init: true,
        data: payload,
      }
    }
    default:
      return state
  }
}
