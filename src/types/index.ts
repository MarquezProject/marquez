import { IDatasetAPI, IJobAPI } from './api'

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
}

export interface IJob extends IJobAPI {
  matches: boolean
}
