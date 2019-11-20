import { all, call, put } from 'redux-saga/effects'
import * as RS from 'redux-saga'
import { INamespaceAPI, INamespacesAPI, IDatasetsAPI, IJobsAPI } from '../types/api'
import { fetchNamespaces, fetchDatasets, fetchJobs } from '../requests'
import { createRollbarMessage } from '../helpers'
import {
  fetchDatasetsSuccess,
  fetchJobsSuccess,
  fetchNamespacesSuccess,
  applicationError
} from '../actionCreators'

export function* fetchNamespacesDatasetsAndJobs() {
  try {
    const response: INamespacesAPI = yield call(fetchNamespaces)
    const { namespaces } = response

    const datasetResponses = yield all(namespaces.map((n: INamespaceAPI) => call(fetchDatasets, n)))

    const jobResponses = yield all(namespaces.map((n: INamespaceAPI) => call(fetchJobs, n)))
    const datasets = datasetResponses.flat()
    const jobs = jobResponses.flat()

    yield put(fetchDatasetsSuccess(datasets))
    yield put(fetchJobsSuccess(jobs))
    yield put(fetchNamespacesSuccess(namespaces))
  } catch (e) {
    createRollbarMessage('fetchNamespacesDatasetsAndJobs', e)
    yield put(applicationError('Something went wrong while fetching initial data.'))
  }
}

export default function* rootSaga(): Generator {
  const sagasThatAreKickedOffImmediately = [fetchNamespacesDatasetsAndJobs()]

  const sagasThatWatchForAction: RS.Saga[] = []
  yield all([...sagasThatAreKickedOffImmediately, ...sagasThatWatchForAction])
}
