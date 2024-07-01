// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { FETCH_ES_SEARCH_JOBS, FETCH_ES_SEARCH_JOBS_SUCCESS } from '../actionCreators/actionTypes'

import { EsSearchResultJobs } from '../../types/api'
import { fetchEsSearchJobs, fetchEsSearchJobsSuccess } from '../actionCreators'

export type IEsSearchJobsState = { isLoading: boolean; data: EsSearchResultJobs; init: boolean }

export const initialState: IEsSearchJobsState = {
  isLoading: false,
  data: { hits: [], highlights: [] },
  init: false,
}

type IJobsAction = ReturnType<typeof fetchEsSearchJobsSuccess> &
  ReturnType<typeof fetchEsSearchJobs>

export default (state = initialState, action: IJobsAction): IEsSearchJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_ES_SEARCH_JOBS:
      return { ...state, isLoading: true }
    case FETCH_ES_SEARCH_JOBS_SUCCESS: {
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
