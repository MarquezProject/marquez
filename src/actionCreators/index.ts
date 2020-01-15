import * as actionTypes from '../constants/ActionTypes'

import { INamespaceAPI, IJobRunAPI } from '../types/api'
import { IFilterByKey, IDataset, IJob } from '../types'

export const fetchDatasetsSuccess = (datasets: IDataset[]) => ({
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

export const fetchJobsSuccess = (jobs: IJob[]) => ({
  type: actionTypes.FETCH_JOBS_SUCCESS,
  payload: {
    jobs
  }
})

export const fetchJobRuns = (jobName: string, namespaceName: string) => ({
  type: actionTypes.FETCH_JOB_RUNS,
  payload: {
    jobName,
    namespaceName
  }
})

export const fetchJobRunsSuccess = (jobName: string, jobRuns: IJobRunAPI[]) => ({
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

export const fetchNamespacesSuccess = (namespaces: INamespaceAPI[]) => ({
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
