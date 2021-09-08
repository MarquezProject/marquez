import * as actionTypes from '../../store/actionCreators/actionTypes'
import datasetsReducer, { initialState } from '../../store/reducers/datasets'

const datasets = require('../../../docker/db/data/datasets.json')

describe('datasets reducer', () => {
  it('should return the initial state', () => {
    expect(datasetsReducer(undefined, {})).toEqual(initialState)
  })

  it('should handle FETCH_DATASETS_SUCCESS', () => {
    const action = {
      type: actionTypes.FETCH_DATASETS_SUCCESS,
      payload: {
        datasets: datasets
      }
    }
    expect(datasetsReducer([], action)).toHaveLength(datasets.length)
  })

})
