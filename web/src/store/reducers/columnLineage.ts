// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { ColumnLineageGraph } from '../../types/api'
import { FETCH_COLUMN_LINEAGE_SUCCESS } from '../actionCreators/actionTypes'

import { setBottomBarHeight, setColumnLineageGraphDepth, setSelectedNode } from '../actionCreators'

export interface IColumnLineageState {
  columnLineage: ColumnLineageGraph
}

const initialState: IColumnLineageState = {
  columnLineage: { graph: [] },
}

type IColumnLineageActions = ReturnType<typeof setSelectedNode> &
  ReturnType<typeof setBottomBarHeight> &
  ReturnType<typeof setColumnLineageGraphDepth>

export default (state = initialState, action: IColumnLineageActions) => {
  switch (action.type) {
    case FETCH_COLUMN_LINEAGE_SUCCESS:
      return { ...state, columnLineage: action.payload }
    default:
      return state
  }
}
