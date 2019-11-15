import { IDataset } from '../types/'
import { findMatchingEntities } from './'
import { FETCH_DATASETS_SUCCESS, FIND_MATCHING_ENTITIES } from '../constants/ActionTypes'
import {
  fetchDatasetsSuccess,
  findMatchingEntities as findMatchingEntitiesActionCreator
} from '../actionCreators'

export type IDatasetsState = IDataset[]

export const initialState: IDatasetsState = []

type IDatasetsAction = ReturnType<typeof fetchDatasetsSuccess> &
  ReturnType<typeof findMatchingEntitiesActionCreator>

export default (state: IDatasetsState = initialState, action: IDatasetsAction): IDatasetsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_DATASETS_SUCCESS:
      return payload.datasets.map(d => ({ ...d, matches: true }))
    case FIND_MATCHING_ENTITIES:
      return findMatchingEntities(payload.search, state) as IDatasetsState
    default:
      return state
  }
}
