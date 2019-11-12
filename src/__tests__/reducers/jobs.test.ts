import jobsReducer, { initialState } from '../../reducers/jobs'
import * as actionTypes from '../../constants/ActionTypes'

const jobs = require('../../../docker/db/data/jobs.json')

describe('jobs reducer', () => {
  it('should return the initial state', () => {
    expect(jobsReducer(undefined, {})).toEqual(initialState)
  })

  it('should handle FETCH_JOBS_SUCCESS', () => {
    const action = {
      type: actionTypes.FETCH_JOBS_SUCCESS,
      payload: {
        jobs: jobs
      }
    }
    expect(jobsReducer([], action)).toHaveLength(jobs.length)
  })
})
