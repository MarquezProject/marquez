import * as actionTypes from '../../store/actionCreators/actionTypes'
import _find from 'lodash/find'
import _sample from 'lodash/sample'
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

  it('should handle FETCH_JOB_RUNS_SUCCESS', () => {
    const randomJob = _sample(jobs)

    const sampleRuns = [
      {
        runId: '12345',
        runState: 'NEW'
      },
      {
        runId: '23456',
        runState: 'RUNNING'
      },
      {
        runId: '34567',
        runState: 'FAILED'
      }
    ]

    const action = {
      type: actionTypes.FETCH_JOB_RUNS_SUCCESS,
      payload: {
        jobRuns: sampleRuns,
        jobName: randomJob.name
      }
    }

    const newState = jobsReducer({isLoading: false, init: true, result: jobs}, action)
    const changedJob = _find(newState.result, j => j.name === randomJob.name)
    expect(changedJob).toHaveProperty('latestRuns')
  })
})
