// SPDX-License-Identifier: Apache-2.0

import * as actionTypes from '../../store/actionCreators/actionTypes'
import jobsReducer, { initialState } from '../../store/reducers/jobs'
import { stopWatchDuration } from "../../helpers/time";

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

describe('stopWatchDuration', () => {
  const oneMinute = 60 * 1000;
  const oneHour = 60 * oneMinute;
  const oneDay = 24 * oneHour;

  it('more than one week', () => {
    const value = stopWatchDuration(oneDay * 9)
    expect("9d 0h 0m 0s").toBe(value);
  })

  it('more than one day', () => {
    const value = stopWatchDuration(oneDay + oneHour)
    expect("1d 1h 0m 0s").toBe(value);
  })

  it('less than one day', () => {
    const value = stopWatchDuration(oneDay - 1000);
    expect("23h 59m 59s").toBe(value);
  })

  it('less than one hour', () => {
    const value = stopWatchDuration(oneHour - 1000);
    expect("59m 59s").toBe(value);
  })

  it('less than one minute', () => {
    const value = stopWatchDuration(oneMinute - 1000);
    expect("0m 59s").toBe(value);
  })

  it('less than one second', () => {
    const value = stopWatchDuration(999);
    expect("999 ms").toBe(value);
  })

  it('no time', () => {
    const value = stopWatchDuration(0);
    expect("0").toBe(value);
  })

})
