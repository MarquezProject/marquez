import datasetsReducer, { initialState } from '../../reducers/datasets'
import * as actionTypes from '../../constants/ActionTypes'

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

  const searchTerms = ['', 'absoluteBogus', datasets[0].name]
  searchTerms.forEach(s => {
    it('should handle FIND_MATCHING_ENTITIES action if it finds all matches, no matches, and some matches (respectively)', () => {
      const action = {
        type: actionTypes.FIND_MATCHING_ENTITIES,
        payload: {
          search: s
        }
      }
      expect(datasetsReducer(datasets, action)).toHaveLength(datasets.length)
    })
  })
})
