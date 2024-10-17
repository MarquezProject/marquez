// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Effects from 'redux-saga/effects'
import {
  ADD_DATASET_FIELD_TAG,
  ADD_DATASET_TAG,
  ADD_JOB_TAG,
  ADD_TAGS,
  DELETE_DATASET,
  DELETE_DATASET_FIELD_TAG,
  DELETE_DATASET_TAG,
  DELETE_JOB,
  DELETE_JOB_TAG,
  FETCH_COLUMN_LINEAGE,
  FETCH_DATASET,
  FETCH_DATASETS,
  FETCH_DATASET_METRICS,
  FETCH_DATASET_VERSIONS,
  FETCH_EVENTS,
  FETCH_INITIAL_DATASET_VERSIONS,
  FETCH_JOB,
  FETCH_JOBS,
  FETCH_JOBS_BY_STATE,
  FETCH_JOB_FACETS,
  FETCH_JOB_METRICS,
  FETCH_LATEST_RUNS,
  FETCH_LINEAGE,
  FETCH_LINEAGE_METRICS,
  FETCH_OPEN_SEARCH_DATASETS,
  FETCH_OPEN_SEARCH_JOBS,
  FETCH_RUNS,
  FETCH_RUN_FACETS,
  FETCH_SEARCH,
  FETCH_SOURCE_METRICS,
} from '../actionCreators/actionTypes'
import {
  ColumnLineageGraph,
  Dataset,
  DatasetVersions,
  Datasets,
  Events,
  Facets,
  Jobs,
  LineageGraph,
  Namespaces,
  OpenSearchResultDatasets,
  OpenSearchResultJobs,
  Runs,
  Tags,
} from '../../types/api'
import { all, put, take } from 'redux-saga/effects'

const call: any = Effects.call

import { Job } from '../../types/api'
import { Search } from '../../types/api'

import { IntervalMetric, getIntervalMetrics } from '../requests/intervalMetrics'
import { LineageMetric, getLineageMetrics } from '../requests/lineageMetrics'
import {
  addDatasetFieldTag,
  addDatasetTag,
  addJobTag,
  addTags,
  deleteDataset,
  deleteDatasetFieldTag,
  deleteDatasetTag,
  deleteJob,
  deleteJobTag,
  getDataset,
  getDatasetVersions,
  getDatasets,
  getEvents,
  getJob,
  getJobFacets,
  getJobs,
  getJobsByState,
  getNamespaces,
  getRunFacets,
  getRuns,
  getTags,
} from '../requests'
import {
  addDatasetFieldTagSuccess,
  addDatasetTagSuccess,
  addJobTagSuccess,
  addTagsSuccess,
  applicationError,
  deleteDatasetFieldTagSuccess,
  deleteDatasetSuccess,
  deleteDatasetTagSuccess,
  deleteJobSuccess,
  deleteJobTagSuccess,
  fetchColumnLineageSuccess,
  fetchDatasetMetricsSuccess,
  fetchDatasetSuccess,
  fetchDatasetVersionsSuccess,
  fetchDatasetsSuccess,
  fetchEventsSuccess,
  fetchFacetsSuccess,
  fetchInitialDatasetVersionsSuccess,
  fetchJobMetricsSuccess,
  fetchJobSuccess,
  fetchJobsSuccess,
  fetchLatestRunsSuccess,
  fetchLineageMetricsSuccess,
  fetchLineageSuccess,
  fetchNamespacesSuccess,
  fetchOpenSearchDatasetsSuccess,
  fetchOpenSearchJobsSuccess,
  fetchRunsSuccess,
  fetchSearchSuccess,
  fetchSourceMetricsSuccess,
  fetchTagsSuccess,
} from '../actionCreators'
import { getColumnLineage } from '../requests/columnlineage'
import { getLineage } from '../requests/lineage'
import { getOpenSearchDatasets, getOpenSearchJobs, getSearch } from '../requests/search'

export function* fetchTags() {
  try {
    const response: Tags = yield call(getTags)
    const { tags } = response
    yield put(fetchTagsSuccess(tags))
  } catch (e) {
    yield put(applicationError('Something went wrong while fetching initial data.'))
  }
}

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
      const result: LineageGraph = yield call(
        getLineage,
        payload.nodeType,
        payload.namespace,
        payload.name,
        payload.depth
      )
      yield put(fetchLineageSuccess(result))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching lineage'))
    }
  }
}

export function* fetchColumnLineage() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_COLUMN_LINEAGE)
      const result: ColumnLineageGraph = yield call(
        getColumnLineage,
        payload.nodeType,
        payload.namespace,
        payload.name,
        payload.depth
      )
      yield put(fetchColumnLineageSuccess(result))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching lineage'))
    }
  }
}

export function* fetchSearch() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_SEARCH)
      const result: Search = yield call(getSearch, payload.q, payload.filter, payload.sort)
      yield put(fetchSearchSuccess(result))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching search'))
    }
  }
}

export function* fetchRunsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_RUNS)
      const response: Runs = yield call(
        getRuns,
        payload.jobName,
        payload.namespace,
        payload.limit,
        payload.offset
      )
      yield put(fetchRunsSuccess(payload.jobName, response.runs, response.totalCount))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching job runs'))
    }
  }
}

export function* fetchLatestRunsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_LATEST_RUNS)
      const response: Runs = yield call(getRuns, payload.jobName, payload.namespace, 14, 0)
      yield put(fetchLatestRunsSuccess(response.runs))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching latest job runs'))
    }
  }
}

export function* fetchJobsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOBS)
      const response: Jobs = yield call(
        getJobs,
        payload.namespace,
        payload.limit,
        payload.offset,
        payload.lastRunStates
      )
      yield put(fetchJobsSuccess(response.jobs, response.totalCount))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching job runs'))
    }
  }
}

export function* fetchJobSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOB)
      const response: Job = yield call(getJob, payload.namespace, payload.job)
      yield put(fetchJobSuccess(response))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching job runs'))
    }
  }
}

export function* deleteJobSaga() {
  while (true) {
    try {
      const { payload } = yield take(DELETE_JOB)
      const job: Job = yield call(deleteJob, payload.namespace, payload.jobName)
      yield put(deleteJobSuccess(job.name))
    } catch (e) {
      yield put(applicationError('Something went wrong while removing job'))
    }
  }
}

export function* fetchDatasetsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_DATASETS)
      const datasets: Datasets = yield call(
        getDatasets,
        payload.namespace,
        payload.limit,
        payload.offset
      )
      yield put(fetchDatasetsSuccess(datasets.datasets, datasets.totalCount))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching dataset runs'))
    }
  }
}

export function* fetchEventsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_EVENTS)
      const events: Events = yield call(
        getEvents,
        payload.after,
        payload.before,
        payload.limit,
        payload.offset
      )
      yield put(fetchEventsSuccess(events))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching event runs'))
    }
  }
}

export function* fetchDatasetSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_DATASET)
      const datasets: Dataset = yield call(getDataset, payload.namespace, payload.name)
      yield put(fetchDatasetSuccess(datasets))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching dataset'))
    }
  }
}

export function* deleteDatasetSaga() {
  while (true) {
    try {
      const { payload } = yield take(DELETE_DATASET)
      const dataset: Dataset = yield call(deleteDataset, payload.namespace, payload.datasetName)
      yield put(deleteDatasetSuccess(dataset.name))
    } catch (e) {
      yield put(applicationError('Something went wrong while removing job'))
    }
  }
}

export function* deleteJobTagSaga() {
  while (true) {
    try {
      const { payload } = yield take(DELETE_JOB_TAG)
      yield call(deleteJobTag, payload.namespace, payload.jobName, payload.tag)
      yield put(deleteJobTagSuccess(payload.namespace, payload.jobName, payload.tag))
    } catch (e) {
      yield put(applicationError('Something went wrong while removing tag from job'))
    }
  }
}

export function* deleteDatasetTagSaga() {
  while (true) {
    try {
      const { payload } = yield take(DELETE_DATASET_TAG)
      yield call(deleteDatasetTag, payload.namespace, payload.datasetName, payload.tag)
      yield put(deleteDatasetTagSuccess(payload.namespace, payload.datasetName, payload.tag))
    } catch (e) {
      yield put(applicationError('Something went wrong while removing tag from dataset'))
    }
  }
}

export function* deleteDatasetFieldTagSaga() {
  while (true) {
    try {
      const { payload } = yield take(DELETE_DATASET_FIELD_TAG)
      yield call(
        deleteDatasetFieldTag,
        payload.namespace,
        payload.datasetName,
        payload.field,
        payload.tag
      )
      yield put(
        deleteDatasetFieldTagSuccess(
          payload.namespace,
          payload.datasetName,
          payload.field,
          payload.tag
        )
      )
    } catch (e) {
      yield put(applicationError('Something went wrong while removing tag from dataset field'))
    }
  }
}

export function* addJobTagSaga() {
  while (true) {
    try {
      const { payload } = yield take(ADD_JOB_TAG)
      yield call(addJobTag, payload.namespace, payload.jobName, payload.tag)
      yield put(addJobTagSuccess(payload.namespace, payload.jobName, payload.tag))
    } catch (e) {
      yield put(applicationError('Something went wrong while adding tag to job'))
    }
  }
}

export function* addDatasetTagSaga() {
  while (true) {
    try {
      const { payload } = yield take(ADD_DATASET_TAG)
      yield call(addDatasetTag, payload.namespace, payload.datasetName, payload.tag)
      yield put(addDatasetTagSuccess(payload.namespace, payload.datasetName, payload.tag))
    } catch (e) {
      yield put(applicationError('Something went wrong while adding tag to dataset'))
    }
  }
}

export function* addDatasetFieldTagSaga() {
  while (true) {
    try {
      const { payload } = yield take(ADD_DATASET_FIELD_TAG)
      yield call(
        addDatasetFieldTag,
        payload.namespace,
        payload.datasetName,
        payload.field,
        payload.tag
      )
      yield put(
        addDatasetFieldTagSuccess(
          payload.namespace,
          payload.datasetName,
          payload.field,
          payload.tag
        )
      )
    } catch (e) {
      yield put(applicationError('Something went wrong while adding tag to dataset field.'))
    }
  }
}

export function* addTagsSaga() {
  while (true) {
    try {
      const { payload } = yield take(ADD_TAGS)
      yield call(addTags, payload.tag, payload.description)
      yield put(addTagsSuccess())
      yield call(fetchTags)
    } catch (e) {
      yield put(applicationError('Something went wrong while adding a tag.'))
    }
  }
}

export function* fetchDatasetVersionsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_DATASET_VERSIONS)
      const response: DatasetVersions = yield call(
        getDatasetVersions,
        payload.namespace,
        payload.name,
        payload.limit,
        payload.offset
      )
      yield put(fetchDatasetVersionsSuccess(response.versions, response.totalCount))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching dataset runs'))
    }
  }
}

export function* fetchInitialDatasetVersionsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_INITIAL_DATASET_VERSIONS)
      const response: DatasetVersions = yield call(
        getDatasetVersions,
        payload.namespace,
        payload.name,
        1,
        0
      )
      yield put(fetchInitialDatasetVersionsSuccess(response.versions, response.totalCount))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching dataset versions'))
    }
  }
}

export function* fetchJobFacetsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOB_FACETS)
      const jobFacets: Facets = yield call(getJobFacets, payload.runId)
      yield put(fetchFacetsSuccess(jobFacets))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching job facets'))
    }
  }
}

export function* fetchRunFacetsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_RUN_FACETS)
      const runFacets: Facets = yield call(getRunFacets, payload.runId)
      yield put(fetchFacetsSuccess(runFacets))
    } catch (e) {
      yield put(applicationError('Something went wrong while fetching run facets'))
    }
  }
}

export function* fetchOpenSearchJobsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_OPEN_SEARCH_JOBS)
      const OpenSearchResultJobs: OpenSearchResultJobs = yield call(getOpenSearchJobs, payload.q)
      yield put(fetchOpenSearchJobsSuccess(OpenSearchResultJobs))
    } catch (e) {
      yield put(applicationError('Something went wrong while searching'))
    }
  }
}

export function* fetchOpenSearchDatasetsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_OPEN_SEARCH_DATASETS)
      const OpenSearchResultDatasets: OpenSearchResultDatasets = yield call(
        getOpenSearchDatasets,
        payload.q
      )
      yield put(fetchOpenSearchDatasetsSuccess(OpenSearchResultDatasets))
    } catch (e) {
      yield put(applicationError('Something went wrong while searching'))
    }
  }
}

export function* fetchLineageMetricsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_LINEAGE_METRICS)
      const lineageMetrics: LineageMetric[] = yield call(getLineageMetrics, payload)
      yield put(fetchLineageMetricsSuccess(lineageMetrics))
    } catch (e) {
      yield put(applicationError('Something went wrong while getting lineage metrics'))
    }
  }
}

export function* fetchJobMetricsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOB_METRICS)
      const intervalMetrics: IntervalMetric[] = yield call(getIntervalMetrics, {
        ...payload,
        asset: 'jobs',
      })
      yield put(fetchJobMetricsSuccess(intervalMetrics))
    } catch (e) {
      yield put(applicationError('Something went wrong while getting job metrics'))
    }
  }
}

export function* fetchDatasetMetricsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_DATASET_METRICS)
      const intervalMetrics: IntervalMetric[] = yield call(getIntervalMetrics, {
        ...payload,
        asset: 'datasets',
      })
      yield put(fetchDatasetMetricsSuccess(intervalMetrics))
    } catch (e) {
      yield put(applicationError('Something went wrong while getting dataset metrics'))
    }
  }
}

export function* fetchSourceMetricsSaga() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_SOURCE_METRICS)
      const intervalMetrics: IntervalMetric[] = yield call(getIntervalMetrics, {
        ...payload,
        asset: 'sources',
      })
      yield put(fetchSourceMetricsSuccess(intervalMetrics))
    } catch (e) {
      yield put(applicationError('Something went wrong while getting source metrics'))
    }
  }
}

export function* fetchJobsByState() {
  while (true) {
    try {
      const { payload } = yield take(FETCH_JOBS_BY_STATE)
      const response: Jobs = yield call(
        getJobsByState,
        payload.runState,
        payload.limit,
        payload.offset
      )
      yield put(fetchJobsSuccess(response.jobs, response.totalCount))
    } catch (e) {
      yield put(applicationError('Something went wrong while getting jobs by state'))
    }
  }
}

export default function* rootSaga(): Generator {
  const sagasThatAreKickedOffImmediately = [fetchNamespaces(), fetchTags()]
  const sagasThatWatchForAction = [
    fetchJobsSaga(),
    fetchRunsSaga(),
    fetchLatestRunsSaga(),
    fetchDatasetsSaga(),
    fetchDatasetSaga(),
    fetchDatasetVersionsSaga(),
    fetchInitialDatasetVersionsSaga(),
    fetchEventsSaga(),
    fetchJobFacetsSaga(),
    fetchRunFacetsSaga(),
    fetchLineage(),
    fetchColumnLineage(),
    fetchSearch(),
    deleteJobSaga(),
    fetchOpenSearchJobsSaga(),
    fetchOpenSearchDatasetsSaga(),
    deleteDatasetSaga(),
    deleteDatasetTagSaga(),
    deleteJobTagSaga(),
    addDatasetTagSaga(),
    addJobTagSaga(),
    deleteDatasetFieldTagSaga(),
    addDatasetFieldTagSaga(),
    addTagsSaga(),
    fetchJobSaga(),
    fetchLineageMetricsSaga(),
    fetchJobsByState(),
    fetchJobMetricsSaga(),
    fetchDatasetMetricsSaga(),
    fetchSourceMetricsSaga(),
  ]

  yield all([...sagasThatAreKickedOffImmediately, ...sagasThatWatchForAction])
}
