// SPDX-License-Identifier: Apache-2.0

import { createNetworkData, formatUpdatedAt } from '../../helpers'
const datasets = require('../../../docker/db/data/datasets.json')
const jobs = require('../../../docker/db/data/jobs.json')

describe('formatUpdated Function', () => {
  it('Should return an empty string when passed a falsey value', () => {
    const updatedAt = ''
    const formatedDate = formatUpdatedAt(updatedAt)
    expect(formatedDate).toBe('')
  })
  it('Should return a datetime string in format like "May 1, 2021 01:45pm"', () => {
    const updatedAt = '2021-05-13T13:45:13Z'
    const formatedDate = formatUpdatedAt(updatedAt)
    expect(formatedDate).toBe('May 13, 2021 01:45pm')
  })
})

