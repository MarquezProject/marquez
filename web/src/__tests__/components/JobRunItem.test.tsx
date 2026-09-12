// Copyright 2018-2026 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Job, Run } from '../../types/api'
import { MemoryRouter } from 'react-router-dom'
import { render, screen } from '@testing-library/react'
import JobRunItem from '../../routes/dashboard/JobRunItem'
import React from 'react'

const run: Run = {
  id: 'run1',
  createdAt: '2024-01-01T00:00:00Z',
  updatedAt: '2024-01-01T00:00:01Z',
  nominalStartTime: '2024-01-01T00:00:00Z',
  nominalEndTime: '2024-01-01T00:00:01Z',
  startedAt: '2024-01-01T00:00:00Z',
  endedAt: '2024-01-01T00:00:01Z',
  durationMs: 1000,
  state: 'COMPLETED',
  jobVersion: { name: 'job1', namespace: 'ns1', version: 'v1' },
  args: {},
  facets: {},
}

const job: Job = {
  id: { name: 'job1', namespace: 'ns1' },
  name: 'job1',
  namespace: 'ns1',
  type: 'BATCH',
  createdAt: run.createdAt,
  updatedAt: run.updatedAt,
  inputs: [],
  outputs: [],
  location: '',
  description: '',
  latestRun: run,
  latestRuns: [],
  tags: [],
  parentJobName: null,
  parentJobUuid: null,
}

test.each([
  { latestRuns: [], height: undefined },
  { latestRuns: [run], height: '40px' },
  { latestRuns: [{ ...run, durationMs: 0 }], height: '0px' },
])('renders job history $latestRuns', ({ latestRuns, height }) => {
  render(
    <MemoryRouter>
      <JobRunItem job={{ ...job, latestRuns }} />
    </MemoryRouter>
  )

  expect(screen.getByText('job1')).toBeTruthy()
  const chart = screen.getByText('LAST 10 RUNS').nextElementSibling!
  expect(chart.children).toHaveLength(10)
  if (height !== undefined) {
    expect(window.getComputedStyle(chart.children[9]).height).toBe(height)
  }
})

test('scales run bars relative to the longest run without reordering the input', () => {
  const latestRuns = [{ ...run, id: 'long', durationMs: 2000 }, run]
  render(
    <MemoryRouter>
      <JobRunItem job={{ ...job, latestRuns }} />
    </MemoryRouter>
  )

  const chart = screen.getByText('LAST 10 RUNS').nextElementSibling!
  expect(window.getComputedStyle(chart.children[8]).height).toBe('20px')
  expect(window.getComputedStyle(chart.children[9]).height).toBe('40px')
  expect(latestRuns.map(({ id }) => id)).toEqual(['long', 'run1'])
})
