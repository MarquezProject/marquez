// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { FETCH_ES_SEARCH, FETCH_ES_SEARCH_SUCCESS } from '../actionCreators/actionTypes'

import { EsSearchResult } from '../../types/api'
import { fetchEsSearch, fetchEsSearchSuccess } from '../actionCreators'

export type IEsSearchState = { isLoading: boolean; data: EsSearchResult; init: boolean }

export const initialState: IEsSearchState = {
  isLoading: false,
  data: { hits: [], highlights: [] },
  init: false,
}

type IJobsAction = ReturnType<typeof fetchEsSearchSuccess> & ReturnType<typeof fetchEsSearch>

export default (state = initialState, action: IJobsAction): IEsSearchState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_ES_SEARCH:
      return { ...state, isLoading: true }
    case FETCH_ES_SEARCH_SUCCESS: {
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
