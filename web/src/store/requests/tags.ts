// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export const getTags = async () => {
  const url = `${API_URL}/tags`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchTags')
}

export const addTags = async (tag: string, description: string) => {
  const url = `${API_URL}/tags/${tag}`
  const payload = {
    description: description,
  }
  return genericFetchWrapper(
    url,
    {
      method: 'PUT',
      body: JSON.stringify(payload),
      headers: {
        'Content-Type': 'application/json',
      },
    },
    'addTags'
  )
}
