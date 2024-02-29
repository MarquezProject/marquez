// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import {
  ADD_TAGS,
  ADD_TAGS_SUCCESS,
  FETCH_TAGS,
  FETCH_TAGS_SUCCESS,
} from '../actionCreators/actionTypes'
import { Tag } from '../../types/api'
import { addTagsSuccess, fetchTagsSuccess } from '../actionCreators'

export type ITagsState = { tags: Tag[] }

export const initialState: ITagsState = {
  tags: [],
}

type ITagsAction = ReturnType<typeof fetchTagsSuccess> & ReturnType<typeof addTagsSuccess>

export default (state: ITagsState = initialState, action: ITagsAction): ITagsState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_TAGS:
      return { ...state }
    case FETCH_TAGS_SUCCESS:
      return { ...state, tags: payload.tags }
    case ADD_TAGS:
      return { ...state }
    case ADD_TAGS_SUCCESS:
      return { ...state }
    default:
      return state
  }
}
