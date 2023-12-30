// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Dataset, DatasetVersions, Datasets } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getDatasets = async (namespace: string, limit = 20, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/datasets?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return {
      datasets: r.datasets.map((d) => ({ ...d, namespace: namespace })),
      totalCount: r.totalCount,
    }
  })
}

export const getDatasetVersions = async (
  namespace: string,
  dataset: string,
  limit = 100,
  offset = 0
) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/datasets/${encodeURIComponent(
    dataset
  )}/versions?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasetVersions').then(
    (versions: DatasetVersions) => versions.versions
  )
}

export const getDataset = async (namespace: string, dataset: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/datasets/${encodeURIComponent(
    dataset
  )}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDataset').then((d: Dataset) => d)
}

export const deleteDataset = async (datasetName: string, namespace: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(namespace)}/datasets/${datasetName}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteDataset')
}

export const deleteDatasetTag = async (namespace: string, datasetName: string, tag: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/datasets/${datasetName}/tags/${tag}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteDatasetTag')
}

export const addDatasetTag = async (namespace: string, datasetName: string, tag: string) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/datasets/${datasetName}/tags/${tag}`
  return genericFetchWrapper(url, { method: 'POST' }, 'addDatasetTag')
}
