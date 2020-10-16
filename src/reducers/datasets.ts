import {
  FETCH_DATASETS_SUCCESS,
  FILTER_DATASETS,
  FIND_MATCHING_ENTITIES
} from '../constants/ActionTypes'
import { IDataset } from '../types'
import {
  fetchDatasetsSuccess,
  filterDatasets,
  findMatchingEntities as findMatchingEntitiesActionCreator
} from '../actionCreators'
import { filterEntities, findMatchingEntities } from './'

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
