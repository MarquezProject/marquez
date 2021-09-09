import * as Effects from 'redux-saga/effects'
import {
  FETCH_DATASETS,
  FETCH_JOBS,
  FETCH_JOB_RUNS,
  FETCH_LINEAGE,
  FETCH_SEARCH
} from '../actionCreators/actionTypes'
import { Namespaces } from '../../types/api'
import { all, put, take } from 'redux-saga/effects'

const call: any = Effects.call

import {
  applicationError,
  fetchDatasetsSuccess,
  fetchJobRunsSuccess,
  fetchJobsSuccess,
  fetchLineageSuccess,
  fetchNamespacesSuccess,
  fetchSearchSuccess
} from '../actionCreators'
import { getDatasets, getJobs, getLatestJobRuns, getNamespaces } from '../requests'
import { getLineage } from '../requests/lineage'
import { getSearch } from '../requests/search'
import _orderBy from 'lodash/orderBy'

export function* fetchNamespaces() {
  try {
    const response: Namespaces = yield call(getNamespaces)
    const { namespaces } = response
    yield put(fetchNamespacesSuccess(namespaces))
  } catch (e) {
    yield put(applicationError('Something went wrong while fetching initial data.'))
  }
}

export function* fetchLineage() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_LINEAGE)
      const result = yield call(getLineage, payload.nodeType, payload.namespace, payload.name)
      yield put(fetchLineageSuccess(result))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching lineage'))
    }
  }
}

export function* fetchSearch() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_SEARCH)
      const result = yield call(getSearch, payload.q, payload.filter, payload.sort)
      yield put(fetchSearchSuccess(result))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching search'))
    }
  }
}

export function* fetchJobRunsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOB_RUNS)
      const { runs } = yield call(getLatestJobRuns, payload.jobName, payload.namespace)
      const runsOrderedByStartTime = _orderBy(runs, ['nominalStartTime'], ['asc'])
      yield put(fetchJobRunsSuccess(payload.jobName, runsOrderedByStartTime))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching job runs'))
    }
  }
}

export function* fetchJobsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOBS)
      const jobs = yield call(getJobs, payload.namespace)
      yield put(fetchJobsSuccess(jobs))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching job runs'))
    }
  }
}

export function* fetchDatasetsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_DATASETS)
      const datasets = yield call(getDatasets, payload.namespace)
      yield put(fetchDatasetsSuccess(datasets))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching dataset runs'))
    }
  }
}

export default function* rootSaga(): Generator {
  const sagasThatAreKickedOffImmediately = [fetchNamespaces()]
  const sagasThatWatchForAction = [
    fetchJobRunsSaga(),
    fetchJobsSaga(),
    fetchDatasetsSaga(),
    fetchLineage(),
    fetchSearch()
  ]

  yield all([...sagasThatAreKickedOffImmediately, ...sagasThatWatchForAction])
}
