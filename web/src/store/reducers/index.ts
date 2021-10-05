import { History } from 'history'
import { Reducer, combineReducers } from 'redux'
import { connectRouter } from 'connected-react-router'
import datasets, { IDatasetsState } from './datasets'
import display, { IDisplayState } from './display'
import jobs, { IJobsState } from './jobs'
import lineage, { ILineageState } from './lineage'
import namespaces, { INamespacesState } from './namespaces'
import runs, { IRunsState } from './runs'
import search, { ISearchState } from './search'

export interface IState {
  datasets: IDatasetsState
  jobs: IJobsState
  runs: IRunsState
  namespaces: INamespacesState
  display: IDisplayState
  router: any
  lineage: ILineageState
  search: ISearchState
}

export default (history: History): Reducer =>
  combineReducers({
    router: connectRouter(history),
    datasets,
    jobs,
    runs,
    namespaces,
    display,
    lineage,
    search
  })
