// SPDX-License-Identifier: Apache-2.0

import * as actionTypes from '../../store/actionCreators/actionTypes'
import jobsReducer, { initialState } from '../../store/reducers/jobs'

const jobs = require('../../../docker/db/data/jobs.json')

describe('jobs reducer', () => {

  it('should handle FETCH_JOBS_SUCCESS', () => {
    const action = {
      type: actionTypes.FETCH_JOBS_SUCCESS,
      payload: {
        jobs: jobs
      }
    }
    expect(jobsReducer(initialState, action)).toStrictEqual({ isLoading: false, result: jobs, init: true })
  })
})
