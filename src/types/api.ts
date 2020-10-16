export interface Tag {
  name: string
  description: string
}
export interface Runs {
  runs: Run[]
}

export interface Runs {
  runs: Run[]
}

export interface Runs {
  runs: Run[]
}

export interface Runs {
  runs: Run[]
}


export interface Namespaces {
  namespaces: Namespace[]
}

export interface Namespace {
  name: string
  createdAt: string
  updatedAt: string
  ownerName: string
  description: string
}

export interface Datasets {
  datasets: Dataset[]
}

export interface Dataset {
  id: DatasetId
  type: DatasetType
  name: string
  physicalName: string
  createdAt: string
  updatedAt: string
  namespace: string
  sourceName: string
  fields: Field[]
  tags: string[]
  lastModifiedAt: string
  description: string
}

export interface DatasetId {
  namespace: string
  name: string
}

export type DatasetType = 'DB_TABLE' | 'STREAM'

export interface Field {
  name: string
  type: string
  tags: string[]
  description: string
}

export interface Jobs {
  jobs: Job[]
}


export interface Job {
  id: JobId
  type: JobType
  name: string
  createdAt: string
  updatedAt: string
  inputs: DatasetId[]
  outputs: DatasetId[]
  namespace: string
  location: string
  context: {
    [key: string]: string
  }
  description: string
  latestRun: Run
}

export interface JobId {
  namespace: string
  name: string
}

export type JobType = 'BATCH' | 'STREAM' | 'SERVICE'

export interface Runs {
  runs: Run[]
}

export interface Run {
  id: string
  createdAt: string
  updatedAt: string
  nominalStartTime: string
  nominalEndTime: string
  state: RunState
  startedAt: string
  endedAt: string
  durationMs: number
  args: {
    [key: string]: string
  }
}

export type RunState = 'NEW' | 'COMPLETED' | 'FAILED' | 'ABORTED'
