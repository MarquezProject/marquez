// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { DatasetVersions, Datasets } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getDatasets = async (namespace: string, limit = 25, offset = 0) => {
  const url = `${API_URL}/namespaces/${encodeURIComponent(
    namespace
  )}/datasets?limit=${limit}&offset=${offset}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchDatasets').then((r: Datasets) => {
    return r.datasets.map(d => ({ ...d, namespace: namespace }))
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
