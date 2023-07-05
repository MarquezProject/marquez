// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'
import Lineage, { LineageProps, getSelectedPaths, initGraph, buildGraphAll } from '../../components/lineage/Lineage'
import { LineageNode } from '../../components/lineage/types'
import { render } from '@testing-library/react'
import { createBrowserHistory } from 'history'
import createSagaMiddleware from 'redux-saga'
import { createRouterMiddleware } from '@lagunovsky/redux-react-router'
import createRootReducer from '../../store/reducers'
import { composeWithDevTools } from '@redux-devtools/extension'
import { applyMiddleware, createStore } from 'redux'
import { Provider } from 'react-redux'
import { MqNode } from '../../components/lineage/types'
import { graphlib } from 'dagre'
import rootSaga from '../../store/sagas'

const mockGraphWithCycle = [
  {
    id: 'job_foo',
    inEdges: [
      {
        origin: 'dataset_foo',
        destination: 'job_foo'
      }
    ],
    outEdges: [
      {
        origin: 'job_foo',
        destination: 'dataset_bar'
      }
    ]
  },
  {
    id: 'dataset_bar',
    inEdges: [
      {
        origin: 'job_foo',
        destination: 'dataset_bar'
      }
    ],
    outEdges: [
      {
        origin: 'dataset_bar',
        destination: 'job_bar'
      }
    ]
  },
  {
    id: 'job_bar',
    inEdges: [
      {
        origin: 'dataset_bar',
        destination: 'job_bar'
      }
    ],
    outEdges: [
      {
        origin: 'job_bar',
        destination: 'dataset_foo'
      }
    ]
  },
  {
    id: 'dataset_foo',
    inEdges: [
      {
        origin: 'job_bar',
        destination: 'dataset_foo'
      }
    ],
    outEdges: [
      {
        origin: 'dataset_foo',
        destination: 'job_foo'
      }
    ]
  }
]

describe('Lineage Component', () => {
  const selectedNode = 'job_foo'
  let g: graphlib.Graph<MqNode>

  beforeEach(() => {
    g = initGraph()
    buildGraphAll(g, mockGraphWithCycle, (gResult: graphlib.Graph<MqNode>) => {
      g = gResult
    })
  })

  it("doesn't follow cycles in the lineage graph", () => {
    const paths = getSelectedPaths(g, selectedNode)

    const pathCounts = paths.reduce((acc, p) => {
      const pathId = p.join(':')
      acc[pathId] = (acc[pathId] || 0) + 1
      return acc
    }, {} as Record<string, number>)

    expect(Object.values(pathCounts).some(c => c > 2)).toBe(false)
  })

  it('renders a valid cycle', () => {
    const actualPaths = getSelectedPaths(g, selectedNode)

    const expectedPaths = [
      ['job_foo', 'dataset_bar'],
      ['dataset_bar', 'job_bar'],
      ['job_bar', 'dataset_foo'],
      ['dataset_foo', 'job_foo'],
      ['dataset_foo', 'job_foo'],
      ['job_bar', 'dataset_foo'],
      ['dataset_bar', 'job_bar'],
      ['job_foo', 'dataset_bar']
    ]

    expect(actualPaths).toEqual(expectedPaths)
  })
})
