// SPDX-License-Identifier: Apache-2.0

import { Namespace } from '../../types/api'

import { FETCH_NAMESPACES_SUCCESS, SELECT_NAMESPACE } from '../actionCreators/actionTypes'
import { Nullable } from '../../types/util/Nullable'
import { fetchNamespacesSuccess, setSelectedNode } from '../actionCreators'

export type INamespacesState = { result: Namespace[]; selectedNamespace: Nullable<string> }
const initialState: INamespacesState = {
  result: [],
  selectedNamespace: null
}

export default (
  state = initialState,
  action: ReturnType<typeof fetchNamespacesSuccess> & ReturnType<typeof setSelectedNode>
) => {
  const { type, payload } = action

  switch (type) {
    case FETCH_NAMESPACES_SUCCESS:
      return {
        result: payload.namespaces,
        selectedNamespace:
          window.localStorage.getItem('selectedNamespace') &&
          action.payload.namespaces.find(
            ns => ns.name === window.localStorage.getItem('selectedNamespace')
          )
            ? window.localStorage.getItem('selectedNamespace')
            : payload.namespaces.length > 0
            ? payload.namespaces[0].name
            : null
      }
    case SELECT_NAMESPACE:
      window.localStorage.setItem('selectedNamespace', action.payload)
      return { ...state, selectedNamespace: action.payload }
    default:
      return state
  }
}
