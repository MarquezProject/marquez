import { HEADER_HEIGHT } from '../helpers/theme'
import { Nullable } from '../types/util/Nullable'
import { SET_BOTTOM_BAR_HEIGHT, SET_SELECTED_NODE } from '../constants/ActionTypes'
import { setBottomBarHeight, setSelectedNode } from '../actionCreators'

export interface ILineageState {
  selectedNode: Nullable<string>
  bottomBarHeight: number
}

const initialState: ILineageState = {
  selectedNode: null,
  bottomBarHeight: (window.innerHeight - HEADER_HEIGHT) / 3
}

type ILineageActions = ReturnType<typeof setSelectedNode> & ReturnType<typeof setBottomBarHeight>

const DRAG_BAR_HEIGHT = 8

export default (state = initialState, action: ILineageActions) => {
  switch (action.type) {
    case SET_SELECTED_NODE:
      return { ...state, selectedNode: action.payload }
    case SET_BOTTOM_BAR_HEIGHT:
      return {
        ...state,
        bottomBarHeight: Math.min(
          window.innerHeight - HEADER_HEIGHT - DRAG_BAR_HEIGHT,
          Math.max(2, action.payload)
        )
      }
    default:
      return state
  }
}
