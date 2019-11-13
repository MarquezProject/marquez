import { IDataset } from '../types/'
import { findMatchingEntities } from './'
import { FETCH_DATASETS_SUCCESS, FIND_MATCHING_ENTITIES } from '../constants/ActionTypes'

export type IDatasetsState = IDataset[]

export const initialState: IDatasetsState = []

interface IDatasetsAction {
  type: string
  payload: {
    datasets?: IDataset[]
    search?: string
  }
}

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
