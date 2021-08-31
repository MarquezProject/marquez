import { History } from 'history'
import { IFilterByKey } from '../../types'
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

// temp fix for: https://github.com/Microsoft/TypeScript/issues/7294#issuecomment-465794460
export function findMatchingEntities(
  payloadSearch: string,
  initialState: Array<any>
): IDatasetsState | IJobsState {
  const searchString = payloadSearch.toLowerCase()
  return initialState.map(e => ({
    ...e,
    matches:
      e.name.toLowerCase().includes(searchString) ||
      (e.description || '').toLowerCase().includes(searchString)
  }))
}

export function filterEntities(
  initialState: Array<any>,
  filterByKey: IFilterByKey,
  filterByValue?: string
): IDatasetsState & IJobsState {
  return initialState.map(e => ({
    ...e,
    matches: filterByKey === 'all' ? true : e[filterByKey] === filterByValue
  }))
}
