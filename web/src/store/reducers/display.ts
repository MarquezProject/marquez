// SPDX-License-Identifier: Apache-2.0

import { APPLICATION_ERROR, DIALOG_TOGGLE } from '../actionCreators/actionTypes'

interface IToggleExpandAction {
  type: string
  payload: {
    message: string
    field: string
  }
}
export interface IDisplayState {
  error: string
  success: string
  dialogIsOpen: boolean
  editWarningField?: string
  isLoading: boolean
}

const initialState: IDisplayState = {
  error: '',
  success: '',
  dialogIsOpen: false,
  editWarningField: '',
  isLoading: true
}

export default (state = initialState, action: IToggleExpandAction) => {
  if (action.type.toLowerCase().includes('success')) {
    return { ...state, isLoading: false }
  } else if (action.type.toLowerCase().includes('fetch')) {
    return { ...state, isLoading: true }
  }
  switch (action.type) {
    case APPLICATION_ERROR:
      return { ...state, error: action.payload.message, success: '' }
    case DIALOG_TOGGLE:
      return { ...state, dialogIsOpen: !state.dialogIsOpen, editWarningField: action.payload.field }
    default:
      return state
  }
}
