import { findMatchingEntities, filterEntities } from '../../reducers'

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

describe('filterEntitites test', () => {
  const datasets = require('../../../docker/db/data/datasets.json')
  const matchingDatasets = filterEntities(datasets, 'sourceName', 'something')
  it('returns an array the same length of input array', () => {
    expect(matchingDatasets.length).toStrictEqual(datasets.length)
  })
  matchingDatasets.forEach(d => {
    it(`each item in returned array has a field called 'matches'`, () => {
      expect(d).toHaveProperty('matches')
    })
  })
  it('returns an array with at least one match if we feed it something that matches', () => {
    const matchingDatasets = filterEntities(datasets, 'sourceName', datasets[0].sourceName)
    expect(matchingDatasets.filter(d => d.matches).length).toBeGreaterThan(0)
  })
  it(`returns an array where every 'matches' field is true if we pass it a filterKey of 'all'`, () => {
    const matchingDatasets = filterEntities(datasets, 'all', datasets[0].sourceName)
    expect(matchingDatasets.filter(d => d.matches).length).toBe(datasets.length)
  })
})
