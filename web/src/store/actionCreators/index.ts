// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as actionTypes from './actionTypes'

import {
  ColumnLineageGraph,
  Dataset,
  DatasetVersion,
  Events,
  Facets,
  Job,
  LineageGraph,
  Namespace,
  OpenSearchResultDatasets,
  OpenSearchResultJobs,
  Run,
  RunState,
  Search,
  Tag,
} from '../../types/api'
import { IntervalMetric } from '../requests/intervalMetrics'
import { JobOrDataset } from '../../types/lineage'
import { LineageMetric } from '../requests/lineageMetrics'
import { Nullable } from '../../types/util/Nullable'

export const fetchEvents = (after: string, before: string, limit: number, offset: number) => ({
  type: actionTypes.FETCH_EVENTS,
  payload: {
    before,
    after,
    limit,
    offset,
  },
})

export const fetchEventsSuccess = (events: Events) => ({
  type: actionTypes.FETCH_EVENTS_SUCCESS,
  payload: {
    events,
  },
})

export const resetEvents = () => ({
  type: actionTypes.RESET_EVENTS,
})

export const fetchDatasets = (namespace: string, limit: number, offset: number) => ({
  type: actionTypes.FETCH_DATASETS,
  payload: {
    namespace,
    limit,
    offset,
  },
})

export const fetchDatasetsSuccess = (datasets: Dataset[], totalCount: number) => ({
  type: actionTypes.FETCH_DATASETS_SUCCESS,
  payload: {
    datasets,
    totalCount,
  },
})

export const fetchDataset = (namespace: string, name: string) => ({
  type: actionTypes.FETCH_DATASET,
  payload: {
    namespace,
    name,
  },
})

export const fetchDatasetSuccess = (dataset: Dataset) => ({
  type: actionTypes.FETCH_DATASET_SUCCESS,
  payload: {
    dataset,
  },
})

export const fetchDatasetVersions = (
  namespace: string,
  name: string,
  limit: number,
  offset: number
) => ({
  type: actionTypes.FETCH_DATASET_VERSIONS,
  payload: {
    namespace,
    name,
    limit,
    offset,
  },
})

export const fetchDatasetVersionsSuccess = (versions: DatasetVersion[], totalCount: number) => ({
  type: actionTypes.FETCH_DATASET_VERSIONS_SUCCESS,
  payload: { versions, totalCount },
})

export const fetchInitialDatasetVersions = (namespace: string, name: string) => ({
  type: actionTypes.FETCH_INITIAL_DATASET_VERSIONS,
  payload: {
    namespace,
    name,
  },
})

export const fetchInitialDatasetVersionsSuccess = (
  versions: DatasetVersion[],
  totalCount: number
) => ({
  type: actionTypes.FETCH_INITIAL_DATASET_VERSIONS_SUCCESS,
  payload: { versions, totalCount },
})

export const resetDatasetVersions = () => ({
  type: actionTypes.RESET_DATASET_VERSIONS,
})

export const resetDataset = () => ({
  type: actionTypes.RESET_DATASET,
})

export const deleteDataset = (datasetName: string, namespace: string) => ({
  type: actionTypes.DELETE_DATASET,
  payload: {
    datasetName,
    namespace,
  },
})

export const deleteDatasetSuccess = (datasetName: string) => ({
  type: actionTypes.DELETE_DATASET_SUCCESS,
  payload: {
    datasetName,
  },
})

export const deleteJobTag = (namespace: string, jobName: string, tag: string) => ({
  type: actionTypes.DELETE_JOB_TAG,
  payload: {
    namespace,
    jobName,
    tag,
  },
})

export const deleteJobTagSuccess = (namespace: string, jobName: string, tag: string) => ({
  type: actionTypes.DELETE_JOB_TAG_SUCCESS,
  payload: {
    jobName,
    namespace,
    tag,
  },
})

export const deleteDatasetTag = (namespace: string, datasetName: string, tag: string) => ({
  type: actionTypes.DELETE_DATASET_TAG,
  payload: {
    namespace,
    datasetName,
    tag,
  },
})

export const deleteDatasetTagSuccess = (namespace: string, datasetName: string, tag: string) => ({
  type: actionTypes.DELETE_DATASET_TAG_SUCCESS,
  payload: {
    datasetName,
    namespace,
    tag,
  },
})

export const deleteDatasetFieldTag = (
  namespace: string,
  datasetName: string,
  tag: string,
  field: string
) => ({
  type: actionTypes.DELETE_DATASET_FIELD_TAG,
  payload: {
    namespace,
    datasetName,
    tag,
    field,
  },
})

export const deleteDatasetFieldTagSuccess = (
  namespace: string,
  datasetName: string,
  field: string,
  tag: string
) => ({
  type: actionTypes.DELETE_DATASET_FIELD_TAG_SUCCESS,
  payload: {
    datasetName,
    namespace,
    tag,
    field,
  },
})

export const addJobTag = (namespace: string, jobName: string, tag: string) => ({
  type: actionTypes.ADD_JOB_TAG,
  payload: {
    namespace,
    jobName,
    tag,
  },
})

export const addJobTagSuccess = (namespace: string, jobName: string, tag: string) => ({
  type: actionTypes.ADD_JOB_TAG_SUCCESS,
  payload: {
    jobName,
    namespace,
    tag,
  },
})

export const addDatasetTag = (namespace: string, datasetName: string, tag: string) => ({
  type: actionTypes.ADD_DATASET_TAG,
  payload: {
    namespace,
    datasetName,
    tag,
  },
})

export const addDatasetTagSuccess = (namespace: string, datasetName: string, tag: string) => ({
  type: actionTypes.ADD_DATASET_TAG_SUCCESS,
  payload: {
    datasetName,
    namespace,
    tag,
  },
})

export const addDatasetFieldTag = (
  namespace: string,
  datasetName: string,
  tag: string,
  field: string
) => ({
  type: actionTypes.ADD_DATASET_FIELD_TAG,
  payload: {
    namespace,
    datasetName,
    tag,
    field,
  },
})

export const addDatasetFieldTagSuccess = (
  namespace: string,
  datasetName: string,
  field: string,
  tag: string
) => ({
  type: actionTypes.ADD_DATASET_FIELD_TAG_SUCCESS,
  payload: {
    datasetName,
    namespace,
    field,
    tag,
  },
})

export const resetDatasets = () => ({
  type: actionTypes.RESET_DATASETS,
})

export const fetchJobs = (
  namespace: Nullable<string>,
  limit: number,
  offset: number,
  lastRunStates?: RunState
) => ({
  type: actionTypes.FETCH_JOBS,
  payload: {
    namespace,
    limit,
    offset,
    lastRunStates,
  },
})

export const fetchJobsSuccess = (jobs: Job[], totalCount: number) => ({
  type: actionTypes.FETCH_JOBS_SUCCESS,
  payload: {
    jobs,
    totalCount,
  },
})

export const fetchJob = (namespace: string, job: string) => ({
  type: actionTypes.FETCH_JOB,
  payload: {
    namespace,
    job,
  },
})

export const fetchJobSuccess = (job: Job) => ({
  type: actionTypes.FETCH_JOB_SUCCESS,
  payload: {
    job,
  },
})

export const resetJobs = () => ({
  type: actionTypes.RESET_JOBS,
})

export const deleteJob = (jobName: string, namespace: string) => ({
  type: actionTypes.DELETE_JOB,
  payload: {
    jobName,
    namespace,
  },
})

export const deleteJobSuccess = (jobName: string) => ({
  type: actionTypes.DELETE_JOB_SUCCESS,
  payload: {
    jobName,
  },
})

export const fetchRuns = (jobName: string, namespace: string, limit: number, offset: number) => ({
  type: actionTypes.FETCH_RUNS,
  payload: {
    jobName,
    namespace,
    limit,
    offset,
  },
})

export const fetchRunsSuccess = (jobName: string, jobRuns: Run[], totalCount: number) => ({
  type: actionTypes.FETCH_RUNS_SUCCESS,
  payload: {
    jobName,
    runs: jobRuns,
    totalCount,
  },
})

export const fetchLatestRuns = (jobName: string, namespace: string) => ({
  type: actionTypes.FETCH_LATEST_RUNS,
  payload: {
    jobName,
    namespace,
  },
})

export const fetchLatestRunsSuccess = (jobRuns: Run[]) => ({
  type: actionTypes.FETCH_LATEST_RUNS_SUCCESS,
  payload: {
    runs: jobRuns,
  },
})

export const fetchRunFacets = (runId: string) => ({
  type: actionTypes.FETCH_RUN_FACETS,
  payload: {
    runId,
  },
})

export const fetchJobFacets = (runId: string) => ({
  type: actionTypes.FETCH_JOB_FACETS,
  payload: {
    runId,
  },
})

export const fetchFacetsSuccess = (facets: Facets) => ({
  type: actionTypes.FETCH_FACETS_SUCCESS,
  payload: {
    facets: facets.facets,
  },
})

export const resetFacets = () => ({
  type: actionTypes.RESET_FACETS,
})

export const resetRuns = () => ({
  type: actionTypes.RESET_RUNS,
})

export const fetchNamespacesSuccess = (namespaces: Namespace[]) => ({
  type: actionTypes.FETCH_NAMESPACES_SUCCESS,
  payload: {
    namespaces,
  },
})

export const fetchTags = () => ({
  type: actionTypes.FETCH_TAGS,
})

export const fetchTagsSuccess = (tags: Tag[]) => ({
  type: actionTypes.FETCH_TAGS_SUCCESS,
  payload: {
    tags,
  },
})

export const addTags = (tag: string, description: string) => ({
  type: actionTypes.ADD_TAGS,
  payload: {
    tag,
    description,
  },
})

export const addTagsSuccess = () => ({
  type: actionTypes.ADD_TAGS_SUCCESS,
})

export const applicationError = (message: string) => ({
  type: actionTypes.APPLICATION_ERROR,
  payload: {
    message,
  },
})

export const dialogToggle = (field: string) => ({
  type: actionTypes.DIALOG_TOGGLE,
  payload: {
    field,
  },
})

export const setSelectedNode = (node: string) => ({
  type: actionTypes.SET_SELECTED_NODE,
  payload: node,
})

export const setBottomBarHeight = (height: number) => ({
  type: actionTypes.SET_BOTTOM_BAR_HEIGHT,
  payload: height,
})

export const setTabIndex = (index: number) => ({
  type: actionTypes.SET_TAB_INDEX,
  payload: index,
})

export const fetchLineage = (
  nodeType: JobOrDataset,
  namespace: string,
  name: string,
  depth: number
) => ({
  type: actionTypes.FETCH_LINEAGE,
  payload: {
    nodeType,
    namespace,
    name,
    depth,
  },
})

export const fetchColumnLineage = (
  nodeType: JobOrDataset,
  namespace: string,
  name: string,
  depth: number
) => ({
  type: actionTypes.FETCH_COLUMN_LINEAGE,
  payload: {
    nodeType,
    namespace,
    name,
    depth,
  },
})

export const fetchLineageSuccess = (lineage: LineageGraph) => ({
  type: actionTypes.FETCH_LINEAGE_SUCCESS,
  payload: lineage,
})

export const fetchColumnLineageSuccess = (columnLineage: ColumnLineageGraph) => ({
  type: actionTypes.FETCH_COLUMN_LINEAGE_SUCCESS,
  payload: columnLineage,
})

export const resetLineage = () => ({
  type: actionTypes.RESET_LINEAGE,
})

export const setLineageGraphDepth = (depth: number) => ({
  type: actionTypes.SET_LINEAGE_GRAPH_DEPTH,
  payload: depth,
})

export const setShowFullGraph = (showFullGraph: boolean) => ({
  type: actionTypes.SET_SHOW_FULL_GRAPH,
  payload: showFullGraph,
})

export const selectNamespace = (namespace: string) => ({
  type: actionTypes.SELECT_NAMESPACE,
  payload: namespace,
})

export const fetchSearch = (q: string, filter: string, sort: string) => ({
  type: actionTypes.FETCH_SEARCH,
  payload: {
    q,
    filter,
    sort,
  },
})

export const fetchSearchSuccess = (search: Search) => ({
  type: actionTypes.FETCH_SEARCH_SUCCESS,
  payload: search,
})

export const setColumnLineageGraphDepth = (depth: number) => ({
  type: actionTypes.SET_COLUMN_LINEAGE_GRAPH_DEPTH,
  payload: depth,
})

export const fetchOpenSearchJobs = (q: string) => ({
  type: actionTypes.FETCH_OPEN_SEARCH_JOBS,
  payload: {
    q,
  },
})

export const fetchOpenSearchJobsSuccess = (search: OpenSearchResultJobs) => ({
  type: actionTypes.FETCH_OPEN_SEARCH_JOBS_SUCCESS,
  payload: search,
})

export const fetchOpenSearchDatasets = (q: string) => ({
  type: actionTypes.FETCH_OPEN_SEARCH_DATASETS,
  payload: {
    q,
  },
})

export const fetchOpenSearchDatasetsSuccess = (search: OpenSearchResultDatasets) => ({
  type: actionTypes.FETCH_OPEN_SEARCH_DATASETS_SUCCESS,
  payload: search,
})

export const fetchLineageMetrics = (unit: 'day' | 'week') => ({
  type: actionTypes.FETCH_LINEAGE_METRICS,
  payload: {
    unit,
  },
})

export const fetchLineageMetricsSuccess = (lineageMetrics: LineageMetric[]) => ({
  type: actionTypes.FETCH_LINEAGE_METRICS_SUCCESS,
  payload: lineageMetrics,
})

export const fetchJobMetrics = (unit: 'day' | 'week') => ({
  type: actionTypes.FETCH_JOB_METRICS,
  payload: {
    unit,
  },
})

export const fetchJobMetricsSuccess = (jobMetrics: IntervalMetric[]) => ({
  type: actionTypes.FETCH_JOB_METRICS_SUCCESS,
  payload: jobMetrics,
})

export const fetchDatasetMetrics = (unit: 'day' | 'week') => ({
  type: actionTypes.FETCH_DATASET_METRICS,
  payload: {
    unit,
  },
})

export const fetchDatasetMetricsSuccess = (datasetMetrics: IntervalMetric[]) => ({
  type: actionTypes.FETCH_DATASET_METRICS_SUCCESS,
  payload: datasetMetrics,
})

export const fetchSourceMetrics = (unit: 'day' | 'week') => ({
  type: actionTypes.FETCH_SOURCE_METRICS,
  payload: {
    unit,
  },
})

export const fetchSourceMetricsSuccess = (sourceMetrics: IntervalMetric[]) => ({
  type: actionTypes.FETCH_SOURCE_METRICS_SUCCESS,
  payload: sourceMetrics,
})

export const fetchJobsByState = (state: RunState, limit: number, offset: number) => ({
  type: actionTypes.FETCH_JOBS_BY_STATE,
  payload: {
    state,
    limit,
    offset,
  },
})

export const fetchJobsByStateSuccess = (jobs: Job[], totalCount: number) => ({
  type: actionTypes.FETCH_JOBS_BY_STATE_SUCCESS,
  payload: {
    jobs,
    totalCount,
  },
})
