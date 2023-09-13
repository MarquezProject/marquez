// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import { FETCH_TAGS, FETCH_TAGS_SUCCESS } from '../actionCreators/actionTypes'
import { Tag } from '../../types/api'
import { fetchTagsSuccess } from '../actionCreators'

export type ITagsState = { isLoading: boolean; tags: Tag[]; init: boolean }

export const initialState: ITagsState = {
  isLoading: false,
  init: false,
  tags: [],
}

type ITagsAction = ReturnType<typeof fetchTagsSuccess>

export default (state: ITagsState = initialState, action: ITagsAction): ITagsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_TAGS:
      return { ...state, isLoading: true }
    case FETCH_TAGS_SUCCESS:
      return { ...state, isLoading: false, init: true, tags: payload.tags }
    default:
      return state
  }
}
