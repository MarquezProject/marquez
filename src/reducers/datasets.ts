import { IDataset } from '../types'
import { findMatchingEntities, filterEntities } from './'
import {
  FETCH_DATASETS_SUCCESS,
  FIND_MATCHING_ENTITIES,
  FILTER_DATASETS
} from '../constants/ActionTypes'
import {
  fetchDatasetsSuccess,
  findMatchingEntities as findMatchingEntitiesActionCreator,
  filterDatasets
} from '../actionCreators'

export type IDatasetsState = IDataset[]

export const initialState: IDatasetsState = []

type IDatasetsAction = ReturnType<typeof fetchDatasetsSuccess> &
  ReturnType<typeof findMatchingEntitiesActionCreator> &
  ReturnType<typeof filterDatasets>

export default (state: IDatasetsState = initialState, action: IDatasetsAction): IDatasetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_DATASETS_SUCCESS:
      return payload.datasets.map(d => ({ ...d, matches: true }))
    case FIND_MATCHING_ENTITIES:
      return findMatchingEntities(payload.search, state) as IDatasetsState
    case FILTER_DATASETS:
      return filterEntities(state, payload.filterByKey, payload.filterByValue)
    default:
      return state
  }
}
