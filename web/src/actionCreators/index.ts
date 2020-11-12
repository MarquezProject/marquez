import * as actionTypes from '../constants/ActionTypes'

import { Dataset, Job, Namespace, Run } from '../types/api'
import { IFilterByKey } from '../types'

export const fetchDatasetsSuccess = (datasets: Dataset[]) => ({
  type: actionTypes.FETCH_DATASETS_SUCCESS,
  payload: {
    datasets
  }
})

export const filterDatasets = (filterByKey: IFilterByKey, filterByValue?: string) => ({
  type: actionTypes.FILTER_DATASETS,
  payload: {
    filterByKey,
    filterByValue
  }
})

export const fetchJobsSuccess = (jobs: Job[]) => ({
  type: actionTypes.FETCH_JOBS_SUCCESS,
  payload: {
    jobs
  }
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

export const filterJobs = (filterByKey: IFilterByKey, filterByValue?: string) => ({
  type: actionTypes.FILTER_JOBS,
  payload: {
    filterByKey,
    filterByValue
  }
})

export const fetchNamespacesSuccess = (namespaces: Namespace[]) => ({
  type: actionTypes.FETCH_NAMESPACES_SUCCESS,
  payload: {
    namespaces
  }
})

export const findMatchingEntities = (search: string) => ({
  type: actionTypes.FIND_MATCHING_ENTITIES,
  payload: {
    search
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
