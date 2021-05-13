import { createNetworkData, formatUpdatedAt } from '../../helpers'
const datasets = require('../../../docker/db/data/datasets.json')
const jobs = require('../../../docker/db/data/jobs.json')

describe('formatUpdated Function', () => {
  it('Should return an empty string when passed a falsey value', () => {
    const updatedAt = ''
    const formatedDate = formatUpdatedAt(updatedAt)
    expect(formatedDate).toBe('')
  })
  it('Should return a datetime string in format like "May 1, 2021 01:45pm"', () => {
    const updatedAt = '2021-05-13T13:45:13.456+07:00'
    const formatedDate = formatUpdatedAt(updatedAt)
    expect(formatedDate).toBe('May 13, 2021 01:45pm')
  })
})

describe('createNetworkData helper test', () => {
  const networkData = createNetworkData(datasets, jobs)
  it('should return an object with a nodes key and a links key', () => {
    expect(networkData).toHaveProperty('nodes')
    expect(networkData).toHaveProperty('links')
  })
  it('should return as many nodes as there are jobs + datasets', () => {
    expect(networkData.nodes).toHaveLength(datasets.length + jobs.length)
  })
  it('should return as many links as there are jobs\' inputs & outputs', () => {
    const linkCount = jobs.reduce((links, job) => {
      return (links += job.inputs.length + job.outputs.length)
    }, 0)
    expect(networkData.links).toHaveLength(linkCount)
  })
  it('each link should have a \'connectsToMatchingNode\'', () => {
    networkData.links.every(l => {
      expect(l).toHaveProperty('connectsToMatchingNode')
    })
  })
})
