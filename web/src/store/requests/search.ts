// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export const getSearch = async (q: string) => {
  const url = `${API_URL}/search/elastic/${q}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchSearch')
}
