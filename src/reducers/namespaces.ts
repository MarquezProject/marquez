import { Namespace } from '../types/api'

import { FETCH_NAMESPACES_SUCCESS } from '../constants/ActionTypes'

export type INamespacesState = Namespace[]
const initialState: INamespacesState = []

interface INamespacesAction {
  type: string
  payload: {
    namespaces: Namespace[]
  }
}

export default (state = initialState, action: INamespacesAction) => {
  const { type, payload } = action

  switch (type) {
    case FETCH_NAMESPACES_SUCCESS:
      return payload.namespaces
    default:
      return state
  }
}
