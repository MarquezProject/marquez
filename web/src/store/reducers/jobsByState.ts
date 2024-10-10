import { FETCH_JOBS_BY_STATE, FETCH_JOBS_BY_STATE_SUCCESS } from '../actionCreators/actionTypes'
import { Job } from '../../types/api'
import { fetchJobsByState, fetchJobsByStateSuccess } from '../actionCreators'

export interface IJobsByStateState {
  jobs: Job[]
  totalCount: number
  loading: boolean
  error: string | null
}

const initialState: IJobsByStateState = {
  jobs: [],
  totalCount: 0,
  loading: false,
  error: null,
}

export type IJobsByStateAction = ReturnType<typeof fetchJobsByState> &
  ReturnType<typeof fetchJobsByStateSuccess>

export default (state = initialState, action: IJobsByStateAction): IJobsByStateState => {
  const { type, payload } = action
  switch (type) {
    case FETCH_JOBS_BY_STATE:
      return {
        ...state,
        loading: true,
        error: null,
      }
    case FETCH_JOBS_BY_STATE_SUCCESS:
      return {
        ...state,
        jobs: payload.jobs,
        totalCount: payload.totalCount,
        loading: false,
      }
    default:
      return state
  }
}
