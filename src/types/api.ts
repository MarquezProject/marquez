interface IFieldsAPI {
  name: string
  type: string
  description: string
}

export interface IDatasetAPI {
  name: string
  createdAt: string
  updatedAt: string
  urn: string
  sourceName: string
  description: string
  tags?: string[]
  fields: IFieldsAPI[]
}

export interface INamespaceAPI {
  name: string
  createdAt: string // timestamp
  ownerName: string
  description: string
}

export interface IJobAPI {
  type: string
  name: string
  createdAt: string
  updatedAt: string
  inputs: string[] // array of dataset urns
  outputs: string[] // array of dataset urns
  location: string
  description: string
  latestRun: IJobRunAPI
  context: {
    SQL: string
  }
}

export interface INamespacesAPI {
  namespaces: INamespaceAPI[]
}
export interface IDatasetsAPI {
  datasets: IDatasetAPI[]
}
export interface IJobsAPI {
  jobs: IJobAPI[]
}

export interface IJobRunAPI {
  runId: string
  createdAt: string // timestamp
  updatedAt: string // timestamp
  nominalStartTime: string // timestamp
  nominalEndTime: string // timestamp
  runState: 'NEW' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'ABORTED'
  runArgs: {
    email: string
    emailOnFailure: boolean
    emailOnRetry: boolean
    retries: number
  }
}
