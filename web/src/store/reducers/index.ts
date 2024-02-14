// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { History } from 'history'
import { IColumnLineageState } from '../requests/columnlineage'
import { Reducer, combineReducers } from 'redux'
import { createRouterReducer } from '@lagunovsky/redux-react-router'
import columnLineage from './columnLineage'
import dataset, { IDatasetState } from './dataset'
import datasetVersions, { IDatasetVersionsState } from './datasetVersions'
import datasets, { IDatasetsState } from './datasets'
import display, { IDisplayState } from './display'
import events, { IEventsState } from './events'
import facets, { IFacetsState } from './facets'
import jobs, { IJobsState } from './jobs'
import lineage, { ILineageState } from './lineage'
import namespaces, { INamespacesState } from './namespaces'
import runs, { IRunsState } from './runs'
import search, { ISearchState } from './search'
import tags, { ITagsState } from './tags'

export interface IState {
  tags: ITagsState
  datasets: IDatasetsState
  dataset: IDatasetState
  datasetVersions: IDatasetVersionsState
  events: IEventsState
  jobs: IJobsState
  runs: IRunsState
  namespaces: INamespacesState
  display: IDisplayState
  router: any
  lineage: ILineageState
  columnLineage: IColumnLineageState
  search: ISearchState
  facets: IFacetsState
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
    runs,
    namespaces,
    display,
    lineage,
    search,
    facets,
    tags,
  })
