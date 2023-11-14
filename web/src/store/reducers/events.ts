// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Event } from '../../types/api'
import { FETCH_EVENTS, FETCH_EVENTS_SUCCESS, RESET_EVENTS } from '../actionCreators/actionTypes'
import { fetchEventsSuccess } from '../actionCreators'

export type IEventsState = {
  isLoading: boolean
  result: Event[]
  totalCount: number
  init: boolean
}

export const initialState: IEventsState = {
  isLoading: false,
  init: false,
  totalCount: 0,
  result: [],
}

type IEventsAction = ReturnType<typeof fetchEventsSuccess>

export default (state: IEventsState = initialState, action: IEventsAction): IEventsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_EVENTS:
      return { ...state, isLoading: true }
    case FETCH_EVENTS_SUCCESS:
      return {
        ...state,
        isLoading: false,
        init: true,
        result: payload.events.events,
        totalCount: payload.events.totalCount,
      }
    case RESET_EVENTS:
      return initialState
    default:
      return state
  }
}
