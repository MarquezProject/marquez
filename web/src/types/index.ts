import { Job, Run } from './api'

export type HttpMethod = 'GET' | 'POST' | 'PATCH' | 'PUT' | 'DELETE'

export type APIError = any

export interface INetworkLink {
  source: string
  target: string
  connectsToMatchingNode: boolean
  offset: 'source' | 'target'
}

export interface INodeNetwork {
  id: string
  tag: 'dataset' | 'job'
}

export interface INetworkData {
  nodes: INodeNetwork[]
  links: INetworkLink[]
}

export interface IJob extends Job {
  latestRuns?: Run[]
}

export type IFilterByDisplay = 'namespace' | 'sourceName'
export type IFilterByKey = 'namespace' | 'sourceName' | 'all'
