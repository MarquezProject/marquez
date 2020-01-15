import jobsReducer, { initialState } from '../../reducers/jobs'
import _sample from 'lodash/sample'
import _find from 'lodash/find'
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

    const newState = jobsReducer(jobs, action)
    const changedJob = _find(newState, j => j.name === randomJob.name)
    expect(changedJob).toHaveProperty('latestRuns')
  })
})
