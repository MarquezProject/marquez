import * as actionTypes from './actionTypes'

import { Dataset, Job, LineageGraph, Namespace, Run, Search } from '../../types/api'
import { JobOrDataset } from '../../components/lineage/types'

export const fetchDatasets = (namespace: string) => ({
  type: actionTypes.FETCH_DATASETS,
  payload: {
    namespace
  }
})

export const fetchDatasetsSuccess = (datasets: Dataset[]) => ({
  type: actionTypes.FETCH_DATASETS_SUCCESS,
  payload: {
    datasets
  }
})

export const resetDatasets = () => ({
  type: actionTypes.RESET_DATASETS
})

export const fetchJobs = (namespace: string) => ({
  type: actionTypes.FETCH_JOBS,
  payload: {
    namespace
  }
})

export const fetchJobsSuccess = (jobs: Job[]) => ({
  type: actionTypes.FETCH_JOBS_SUCCESS,
  payload: {
    jobs
  }
})

export const resetJobs = () => ({
  type: actionTypes.RESET_JOBS
})

export const fetchJobRuns = (jobName: string, namespace: string) => ({
  type: actionTypes.FETCH_JOB_RUNS,
  payload: {
    jobName,
    namespace
  }
})

export const fetchJobRunsSuccess = (jobName: string, jobRuns: Run[]) => ({
  type: actionTypes.FETCH_JOB_RUNS_SUCCESS,
  payload: {
    jobName,
    lastTenJobRuns: jobRuns
  }
})

export const fetchNamespacesSuccess = (namespaces: Namespace[]) => ({
  type: actionTypes.FETCH_NAMESPACES_SUCCESS,
  payload: {
    namespaces
  }
})

export const applicationError = (message: string) => ({
  type: actionTypes.APPLICATION_ERROR,
  payload: {
    message
  }
})

export const dialogToggle = (field: string) => ({
  type: actionTypes.DIALOG_TOGGLE,
  payload: {
    field
  }
})

export const setSelectedNode = (node: string) => ({
  type: actionTypes.SET_SELECTED_NODE,
  payload: node
})

export const setBottomBarHeight = (height: number) => ({
  type: actionTypes.SET_BOTTOM_BAR_HEIGHT,
  payload: height
})

export const fetchLineage = (nodeType: JobOrDataset, namespace: string, name: string) => ({
  type: actionTypes.FETCH_LINEAGE,
  payload: {
    nodeType,
    namespace,
    name
  }
})

export const fetchLineageSuccess = (lineage: LineageGraph) => ({
  type: actionTypes.FETCH_LINEAGE_SUCCESS,
  payload: lineage
})

export const resetLineage = () => ({
  type: actionTypes.RESET_LINEAGE
})

export const selectNamespace = (namespace: string) => ({
  type: actionTypes.SELECT_NAMESPACE,
  payload: namespace
})

export const fetchSearch = (q: string, filter: string, sort: string) => ({
  type: actionTypes.FETCH_SEARCH,
  payload: {
    q,
    filter,
    sort
  }
})

export const fetchSearchSuccess = (search: Search) => ({
  type: actionTypes.FETCH_SEARCH_SUCCESS,
  payload: search
})
