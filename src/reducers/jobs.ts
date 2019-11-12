import { IJob } from '../types/'
import { findMatchingEntities } from './'
import { FETCH_JOBS_SUCCESS, FIND_MATCHING_ENTITIES } from '../constants/ActionTypes'

export type IJobsState = IJob[]

export const initialState: IJobsState = []

interface IJobsAction {
  type: string
  payload: {
    jobs: IJob[]
    search?: string
  }
}

export default (state = initialState, action: IJobsAction): IJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOBS_SUCCESS:
      return payload.jobs.map(j => ({ ...j, matches: true }))
    case FIND_MATCHING_ENTITIES:
      return findMatchingEntities(payload.search, state) as IJobsState
    default:
      return state
  }
}
