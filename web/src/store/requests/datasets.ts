// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Dataset, DatasetVersions, Datasets } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getDatasets = async (namespace: string, limit = 20, offset = 0) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return {
      datasets: r.datasets.map((d) => ({ ...d, namespace: namespace })),
      totalCount: r.totalCount,
    }
  })
}

export const getDatasetVersions = async (
  namespace: string,
  datasetName: string,
  limit: number,
  offset: number
) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}/versions?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasetVersions').then(
    (r: DatasetVersions) => {
      return { versions: r.versions, totalCount: r.totalCount }
    }
  )
}

export const getDataset = async (namespace: string, datasetName: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDataset').then((d: Dataset) => d)
}

export const deleteDataset = async (namespace: string, datasetName: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteDataset')
}

export const deleteDatasetTag = async (namespace: string, datasetName: string, tag: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const encodedTag = encodeURIComponent(tag)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}/tags/${encodedTag}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteDatasetTag')
}

export const addDatasetTag = async (namespace: string, datasetName: string, tag: string) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const encodedTag = encodeURIComponent(tag)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}/tags/${encodedTag}`
  return genericFetchWrapper(url, { method: 'POST' }, 'addDatasetTag')
}

export const deleteDatasetFieldTag = async (
  namespace: string,
  datasetName: string,
  field: string,
  tag: string
) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const encodedField = encodeURIComponent(field)
  const encodedTag = encodeURIComponent(tag)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}/fields/${encodedField}/tags/${encodedTag}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteDatasetFieldTag')
}

export const addDatasetFieldTag = async (
  namespace: string,
  datasetName: string,
  field: string,
  tag: string
) => {
  const encodedNamespace = encodeURIComponent(namespace)
  const encodedDataset = encodeURIComponent(datasetName)
  const encodedField = encodeURIComponent(field)
  const encodedTag = encodeURIComponent(tag)
  const url = `${API_URL}/namespaces/${encodedNamespace}/datasets/${encodedDataset}/fields/${encodedField}/tags/${encodedTag}`
  return genericFetchWrapper(url, { method: 'POST' }, 'addDatasetFieldTag')
}
