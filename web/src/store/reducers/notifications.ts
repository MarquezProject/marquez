// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  ARCHIVE_ALL_NOTIFICATIONS,
  ARCHIVE_ALL_NOTIFICATIONS_SUCCESS,
  ARCHIVE_NOTIFICATION,
  ARCHIVE_NOTIFICATION_SUCCESS,
  FETCH_NOTIFICATIONS,
  FETCH_NOTIFICATIONS_SUCCESS,
} from '../actionCreators/actionTypes'
import { Notification } from '../../types/api'

import {
  archiveAllNotificationsSuccess,
  archiveNotificationSuccess,
  fetchNotificationsSuccess,
} from '../actionCreators'

export interface INotificationState {
  notifications: Notification[]
  isLoading: boolean
}

const initialState: INotificationState = {
  notifications: [],
  isLoading: false,
}

type INotificationActions = ReturnType<typeof archiveAllNotificationsSuccess> &
  ReturnType<typeof fetchNotificationsSuccess> &
  ReturnType<typeof archiveNotificationSuccess>

export default (state = initialState, action: INotificationActions) => {
  switch (action.type) {
    case FETCH_NOTIFICATIONS: {
      return { ...state, isLoading: true }
    }
    case FETCH_NOTIFICATIONS_SUCCESS:
      return { ...state, isLoading: false, notifications: action.payload }
    case ARCHIVE_NOTIFICATION:
      return { ...state, isLoading: true }
    case ARCHIVE_NOTIFICATION_SUCCESS:
      return {
        ...state,
        isLoading: false,
        notifications: state.notifications.filter((a) => a.uuid !== action.payload.uuid),
      }
    case ARCHIVE_ALL_NOTIFICATIONS:
      return { ...state, isLoading: true }
    case ARCHIVE_ALL_NOTIFICATIONS_SUCCESS:
      return { ...state, isLoading: false, notifications: [] }
    default:
      return state
  }
}
