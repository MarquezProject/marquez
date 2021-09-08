import { History } from 'history'
import { Reducer, combineReducers } from 'redux'
import { connectRouter } from 'connected-react-router'
import datasets, { IDatasetsState } from './datasets'
import display, { IDisplayState } from './display'
import jobs, { IJobsState } from './jobs'
import lineage, { ILineageState } from './lineage'
import namespaces, { INamespacesState } from './namespaces'

export interface IState {
  datasets: IDatasetsState
  jobs: IJobsState
  namespaces: INamespacesState
  display: IDisplayState
  router: any
  lineage: ILineageState
}

export default (history: History): Reducer =>
  combineReducers({
    router: connectRouter(history),
    datasets,
    jobs,
    namespaces,
    display,
    lineage
  })
