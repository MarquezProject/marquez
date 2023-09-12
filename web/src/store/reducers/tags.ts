// Import the necessary action types and action creators
import { Tag } from '../../types/api'
import {
    FETCH_TAGS,
    FETCH_TAGS_SUCCESS
  } from '../actionCreators/actionTypes';
  import { fetchTagsSuccess } from '../actionCreators';
  
  // Define the initial state
  export type ITagsState = { isLoading: boolean; tags: Tag[]; init: boolean }
  
  export const initialState: ITagsState = {
    isLoading: false,
    init: false,
    tags: [],
  }
  
  // Define the action type for the fetchTagsSuccess action
  type ITagsAction = ReturnType<typeof fetchTagsSuccess>
  
  // Define the tags reducer
  // Define the tags reducer
export default (
  state: ITagsState = initialState,
  action: ITagsAction
): ITagsState => {
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