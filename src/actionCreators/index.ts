import * as actionTypes from '../constants/ActionTypes'

import { IDatasetAPI, IJobAPI, INamespaceAPI } from '../types/api'

export const fetchDatasetsSuccess = (datasets: IDatasetAPI[]) => ({
  type: actionTypes.FETCH_DATASETS_SUCCESS,
  payload: {
    datasets
  }
})

export const fetchJobsSuccess = (jobs: IJobAPI[]) => ({
  type: actionTypes.FETCH_JOBS_SUCCESS,
  payload: {
    jobs
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
