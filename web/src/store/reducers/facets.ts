// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  FETCH_FACETS_SUCCESS,
  FETCH_JOB_FACETS,
  FETCH_RUN_FACETS,
  RESET_FACETS,
} from '../actionCreators/actionTypes'
import { Facets } from '../../types/api'
import { fetchFacetsSuccess } from '../actionCreators'

export type IFacetsState = { isLoading: boolean; result: object; init: boolean }

export const initialState: IFacetsState = { isLoading: false, result: {} as Facets, init: false }

type IFacetsAction = ReturnType<typeof fetchFacetsSuccess>

export default (state = initialState, action: IFacetsAction): IFacetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOB_FACETS:
    case FETCH_RUN_FACETS:
      return { ...state, isLoading: true }
    case FETCH_FACETS_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload.facets }
    case RESET_FACETS:
      return initialState
    default:
      return state
  }
}
