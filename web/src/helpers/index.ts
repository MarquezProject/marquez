import { IDataset, IJob, INetworkData, INetworkLink, INodeNetwork } from '../types/'
import { isoParse, timeFormat } from 'd3-time-format'
import _find from 'lodash/find'

export const createNetworkData = (datasets: IDataset[], jobs: IJob[]): INetworkData => {
  const datasetNodes: INodeNetwork[] = datasets.map(d => ({
    id: d.name,
    tag: 'dataset',
    matches: d.matches
  }))

  const jobNodes: INodeNetwork[] = jobs.map(j => ({
    id: j.name,
    tag: 'job',
    matches: j.matches
  }))

  const links = jobs.reduce((links, singleJob) => {
    const inLinks = singleJob.inputs.map(input => {
      const matchingDataset = _find(datasets, d => d.name === input.name)
      const connectsToMatchingDataset = matchingDataset && matchingDataset.matches
      const connectsToMatchingJob = singleJob.matches
      return {
        offset: 'source',
        source: input,
        target: singleJob.name,
        connectsToMatchingNode: !!(connectsToMatchingDataset && connectsToMatchingJob)
      }
    })

    const outLinks = singleJob.outputs.map(output => {
      const matchingDataset = _find(datasets, d => d.name === output.name)
      const connectsToMatchingDataset = matchingDataset && matchingDataset.matches
      const connectsToMatchingJob = singleJob.matches
      return {
        offset: 'target',
        source: singleJob.name,
        target: output.name,
        connectsToMatchingNode: !!(connectsToMatchingDataset && connectsToMatchingJob)
      }
    })

    return [...links, ...inLinks, ...outLinks] as INetworkLink[]
  }, [])
  return {
    nodes: [...datasetNodes, ...jobNodes],
    links
  }
}

export const capitalize = (word: string) => {
  if (word.length < 2) {
    return word.toUpperCase()
  }
  return `${word[0].toUpperCase()}${word.slice(1)}`
}

const customTimeFormat = timeFormat('%b %d, %Y %I:%M%p')

export const formatUpdatedAt = (updatedAt: string) => {
  const parsedDate = isoParse(updatedAt)
  if (!parsedDate) {
    return ''
  } else {
    const dateString = customTimeFormat(parsedDate)
    return `${dateString.slice(0, -2)}${dateString.slice(-2).toLowerCase()}`
  }
}
