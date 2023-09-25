// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import {
  FETCH_LINEAGE_SUCCESS,
  RESET_LINEAGE,
  SET_BOTTOM_BAR_HEIGHT,
  SET_LINEAGE_GRAPH_DEPTH,
  SET_SELECTED_NODE,
  SET_SHOW_FULL_GRAPH,
  SET_TAB_INDEX,
} from '../actionCreators/actionTypes'
import { HEADER_HEIGHT } from '../../helpers/theme'
import { LineageGraph } from '../../types/api'
import { Nullable } from '../../types/util/Nullable'
import { setBottomBarHeight, setLineageGraphDepth, setSelectedNode } from '../actionCreators'

export interface ILineageState {
  lineage: LineageGraph
  selectedNode: Nullable<string>
  bottomBarHeight: number
  depth: number
  tabIndex: number
  showFullGraph: boolean
}

const initialState: ILineageState = {
  lineage: { graph: [] },
  selectedNode: null,
  bottomBarHeight: (window.innerHeight - HEADER_HEIGHT) / 3,
  depth: 5,
  tabIndex: 0,
  showFullGraph: true,
}

type ILineageActions = ReturnType<typeof setSelectedNode> &
  ReturnType<typeof setBottomBarHeight> &
  ReturnType<typeof setLineageGraphDepth>

const DRAG_BAR_HEIGHT = 8

export default (state = initialState, action: ILineageActions) => {
  switch (action.type) {
    case FETCH_LINEAGE_SUCCESS:
      return { ...state, lineage: action.payload }
    case SET_SELECTED_NODE:
      // reset the selected index if we are not on the i/o tab
      return { ...state, selectedNode: action.payload, tabIndex: state.tabIndex === 1 ? 1 : 0 }
    case SET_BOTTOM_BAR_HEIGHT:
      return {
        ...state,
        bottomBarHeight: Math.min(
          window.innerHeight - HEADER_HEIGHT - DRAG_BAR_HEIGHT,
          Math.max(2, action.payload)
        ),
      }
    case SET_TAB_INDEX:
      return {
        ...state,
        tabIndex: action.payload,
      }
    case SET_LINEAGE_GRAPH_DEPTH:
      return {
        ...state,
        depth: action.payload,
      }
    case SET_SHOW_FULL_GRAPH:
      return {
        ...state,
        showFullGraph: action.payload,
      }
    case RESET_LINEAGE: {
      return { ...state, lineage: { graph: [] } }
    }
    default:
      return state
  }
}
