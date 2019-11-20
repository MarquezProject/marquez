import { IJob } from '../types/'
import { findMatchingEntities, filterEntities } from './'
import { FETCH_JOBS_SUCCESS, FIND_MATCHING_ENTITIES, FILTER_JOBS } from '../constants/ActionTypes'
import {
  fetchJobsSuccess,
  findMatchingEntities as findMatchingEntitiesActionCreator,
  filterJobs
} from '../actionCreators'

export type IJobsState = IJob[]

export const initialState: IJobsState = []

type IJobsAction = ReturnType<typeof fetchJobsSuccess> &
  ReturnType<typeof findMatchingEntitiesActionCreator> &
  ReturnType<typeof filterJobs>

export default (state = initialState, action: IJobsAction): IJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOBS_SUCCESS:
      return payload.jobs.map(j => ({ ...j, matches: true }))
    case FIND_MATCHING_ENTITIES:
      return findMatchingEntities(payload.search, state) as IJobsState
    case FILTER_JOBS:
      return filterEntities(state, payload.filterByKey, payload.filterByValue)
    default:
      return state
  }
}
