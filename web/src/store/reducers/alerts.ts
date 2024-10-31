// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Alert } from '../../types/api'
import {
  DELETE_ALERT,
  DELETE_ALERT_SUCCESS,
  FETCH_ALERTS,
  FETCH_ALERTS_SUCCESS,
  UPDATE_ALERT,
  UPDATE_ALERT_SUCCESS,
} from '../actionCreators/actionTypes'

import { deleteAlertSuccess, fetchAlertsSuccess, updateAlertSuccess } from '../actionCreators'

export interface IAlertState {
  alerts: Alert[]
  isLoading: boolean
}

const initialState: IAlertState = {
  alerts: [],
  isLoading: false,
}

type IAlertActions = ReturnType<typeof fetchAlertsSuccess> &
  ReturnType<typeof updateAlertSuccess> &
  ReturnType<typeof deleteAlertSuccess>

export default (state = initialState, action: IAlertActions) => {
  switch (action.type) {
    case FETCH_ALERTS: {
      return { ...state, isLoading: true }
    }
    case FETCH_ALERTS_SUCCESS:
      return { ...state, isLoading: false, alerts: action.payload }
    case UPDATE_ALERT:
      return { ...state, isLoading: true }
    case UPDATE_ALERT_SUCCESS:
      return { ...state, isLoading: false, alerts: [...state.alerts, action.payload] }
    case DELETE_ALERT:
      return { ...state, isLoading: true }
    case DELETE_ALERT_SUCCESS:
      return {
        ...state,
        isLoading: false,
        alerts: state.alerts.filter((a) => a.uuid !== action.payload.uuid),
      }
    default:
      return state
  }
}
