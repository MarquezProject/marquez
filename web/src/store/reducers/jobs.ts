import {
  FETCH_JOBS,
  FETCH_JOBS_SUCCESS,
  FETCH_JOB_RUNS_SUCCESS,
  RESET_JOBS
} from '../actionCreators/actionTypes'
import { IJob } from '../../types'
import { fetchJobRunsSuccess, fetchJobsSuccess } from '../actionCreators'

export type IJobsState = { isLoading: boolean; result: IJob[]; init: boolean }

export const initialState: IJobsState = { isLoading: false, result: [], init: false }

type IJobsAction = ReturnType<typeof fetchJobsSuccess> & ReturnType<typeof fetchJobRunsSuccess>

export default (state = initialState, action: IJobsAction): IJobsState => {
  const { type, payload } = action

  switch (type) {
    case FETCH_JOBS:
      return { ...state, isLoading: true }
    case FETCH_JOBS_SUCCESS:
      return { ...state, isLoading: false, init: true, result: payload.jobs }
    case FETCH_JOB_RUNS_SUCCESS: {
      return {
        ...state,
        result: state.result.map((j: IJob) => {
          const isMatching = j.name == payload.jobName
          return isMatching ? { ...j, latestRuns: payload.lastTenJobRuns } : j
        })
      }
    }
    case RESET_JOBS:
      return initialState
    default:
      return state
  }
}
