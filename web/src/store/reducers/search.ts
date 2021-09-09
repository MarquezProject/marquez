import { FETCH_SEARCH, FETCH_SEARCH_SUCCESS } from '../actionCreators/actionTypes'

import { GroupedSearch, GroupedSearchResult } from '../../types/api'
import {
  fetchSearch,
  fetchSearchSuccess
} from '../actionCreators'
import { groupBy } from '../../types/util/groupBy'

export type ISearchState = { isLoading: boolean; result: GroupedSearchResult; init: boolean }

export const initialState: ISearchState = {
  isLoading: false,
  result: { results: new Map<string, GroupedSearch[]>(), rawResults: [] },
  init: false
}

type IJobsAction = ReturnType<typeof fetchSearchSuccess> & ReturnType<typeof fetchSearch>

export default (state = initialState, action: IJobsAction): ISearchState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_SEARCH:
      return { ...state, isLoading: true }
    case FETCH_SEARCH_SUCCESS: {
      const groupedResult = payload.results.map(result => {
        return {
          ...result,
          group: `${result.namespace}:${result.name.substring(0, result.name.lastIndexOf('.'))}`
        }
      })
      return {
        ...state,
        isLoading: false,
        init: true,
        result: {
          results: groupBy(groupedResult, 'group'),
          rawResults: groupedResult
        }
      }
    }
    default:
      return state
  }
}
