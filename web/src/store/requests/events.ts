// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { Events } from '../../types/api'
import { genericFetchWrapper } from './index'

export const getEvents = async (after = '', before = '', limit = 100, sortDirection = 'desc') => {
  const url = `${API_URL}/events/lineage?limit=${limit}&before=${before}&after=${after}&sortDirection=${sortDirection}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchEvents').then((r: Events) => {
    return r.events.map(d => ({ ...d }))
  })
}
