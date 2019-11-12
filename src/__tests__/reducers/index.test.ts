import { findMatchingEntities } from '../../reducers'
import { match } from 'minimatch'
describe('findMatchingEntities test', () => {
  const datasets = require('../../../docker/db/data/datasets.json')
  const matchingDatasets = findMatchingEntities('searchTerm', datasets)
  it('returns an array the same length of input array', () => {
    expect(matchingDatasets.length).toStrictEqual(datasets.length)
  })
  matchingDatasets.forEach(d => {
    it(`each item in returned array has a field called 'matches'`, () => {
      expect(d).toHaveProperty('matches')
    })
  })
})
