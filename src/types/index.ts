import { IDatasetAPI, IJobAPI, IJobRunAPI } from './api'

export type IMethods = 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'

// To-do
export type IAPIError = any

export interface INetworkLink {
  source: string
  target: string
  connectsToMatchingNode: boolean
  offset: 'source' | 'target'
}

export interface INodeNetwork {
  id: string
  tag: 'dataset' | 'job'
  matches: boolean
}

export interface INetworkData {
  nodes: INodeNetwork[]
  links: INetworkLink[]
}

export interface IDataset extends IDatasetAPI {
  matches: boolean
  namespace: string
}

export interface IJob extends IJobAPI {
  matches: boolean
  namespace: string
  latestRuns?: IJobRunAPI[]
}

export type IFilterByDisplay = 'namespace' | 'datasource'
export type IFilterByKey = 'namespace' | 'sourceName' | 'all'
