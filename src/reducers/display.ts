import { APPLICATION_ERROR, DIALOG_TOGGLE } from '../constants/ActionTypes'

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
}

const initialState: IDisplayState = {
  error: '',
  success: '',
  dialogIsOpen: false,
  editWarningField: ''
}

export default (state = initialState, action: IToggleExpandAction) => {
  switch (action.type) {
    case APPLICATION_ERROR:
      return { ...state, error: action.payload.message, success: '' }
    case DIALOG_TOGGLE:
      return { ...state, dialogIsOpen: !state.dialogIsOpen, editWarningField: action.payload.field }
    default:
      return state
  }
}
