// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { FETCH_SEARCH, FETCH_SEARCH_SUCCESS } from '../actionCreators/actionTypes'

import { SearchResult } from '../../types/api'
import { fetchSearch, fetchSearchSuccess } from '../actionCreators'

export type ISearchState = { isLoading: boolean; data: SearchResult; init: boolean }

export const initialState: ISearchState = {
  isLoading: false,
  data: { jobs: [], datasets: [] },
  init: false
}

type IJobsAction = ReturnType<typeof fetchSearchSuccess> & ReturnType<typeof fetchSearch>

export default (state = initialState, action: IJobsAction): ISearchState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_SEARCH:
      return { ...state, isLoading: true }
    case FETCH_SEARCH_SUCCESS: {
      return {
        ...state,
        isLoading: false,
        init: true,
        data: payload
      }
    }
    default:
      return state
  }
}
