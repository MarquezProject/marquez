// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { History } from 'history'
import { IColumnLineageState } from '../requests/columnlineage'
import { Reducer, combineReducers } from 'redux'
import { createRouterReducer } from '@lagunovsky/redux-react-router'
import columnLineage from './columnLineage'
import dataset, { IDatasetState } from './dataset'
import datasetMetrics, { IDatasetMetricsState } from './datasetMetrics'
import datasetVersions, { IDatasetVersionsState } from './datasetVersions'
import datasets, { IDatasetsState } from './datasets'
import display, { IDisplayState } from './display'
import events, { IEventsState } from './events'
import facets, { IFacetsState } from './facets'
import job, { IJobState } from './job'
import jobMetrics, { IJobMetricsState } from './jobMetrics'
import jobs, { IJobsState } from './jobs'
import jobsByState, { IJobsByStateState } from './jobsByState'
import lineage, { ILineageState } from './lineage'
import lineageMetrics, { ILineageMetricsState } from './lineageMetrics'
import namespaces, { INamespacesState } from './namespaces'
import openSearchDatasets, { IOpenSearchDatasetsState } from './openSearchDatasets'
import openSearchJobs, { IOpenSearchJobsState } from './openSearch'
import runs, { IRunsState } from './runs'
import search, { ISearchState } from './search'
import sourceMetrics, { ISourceMetricsState } from './sourceMetrics'
import tags, { ITagsState } from './tags'

export interface IState {
  tags: ITagsState
  datasets: IDatasetsState
  dataset: IDatasetState
  datasetVersions: IDatasetVersionsState
  openSearchJobs: IOpenSearchJobsState
  openSearchDatasets: IOpenSearchDatasetsState
  events: IEventsState
  jobs: IJobsState
  job: IJobState
  runs: IRunsState
  namespaces: INamespacesState
  display: IDisplayState
  router: any
  lineage: ILineageState
  columnLineage: IColumnLineageState
  search: ISearchState
  facets: IFacetsState
  lineageMetrics: ILineageMetricsState
  jobsByState: IJobsByStateState
  jobMetrics: IJobMetricsState
  datasetMetrics: IDatasetMetricsState
  sourceMetrics: ISourceMetricsState
}

export default (history: History): Reducer =>
  combineReducers({
    router: createRouterReducer(history),
    columnLineage,
    dataset,
    datasets,
    datasetVersions,
    events,
    jobs,
    job,
    runs,
    namespaces,
    display,
    lineage,
    search,
    openSearchJobs,
    openSearchDatasets,
    facets,
    tags,
    lineageMetrics,
    jobsByState,
    jobMetrics,
    datasetMetrics,
    sourceMetrics,
  })
