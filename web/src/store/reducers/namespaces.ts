import { Namespace } from '../../types/api'

import { FETCH_NAMESPACES_SUCCESS, SELECT_NAMESPACE } from '../actionCreators/actionTypes'
import { Nullable } from '../../types/util/Nullable'

export type INamespacesState = { result: Namespace[]; selectedNamespace: Nullable<string> }
const initialState: INamespacesState = { result: [], selectedNamespace: null }

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
      return {
        result: payload.namespaces,
        selectedNamespace: payload.namespaces[0] ? payload.namespaces[0].name : null
      }
    case SELECT_NAMESPACE:
      return { ...state, selectedNamespace: action.payload }
    default:
      return state
  }
}
