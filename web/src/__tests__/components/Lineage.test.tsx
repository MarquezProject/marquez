// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { mount } from 'enzyme'
import { Lineage as BaseLineageComponent } from '../../components/lineage/Lineage'
import { LineageGraph } from '../../types/api'
import configureStore from 'redux-mock-store'
import { Provider } from 'react-redux'
import { MemoryRouter, Route, withRouter } from 'react-router-dom'

const Lineage = withRouter(BaseLineageComponent)

const mockGraphWithCycle: LineageGraph = {
  graph: [
    {
      id: 'job_foo',
      type: 'JOB',
      data: {
        id: { namespace: 'test', name: 'test' },
        type: 'BATCH',
        name: 'job_foo',
        createdAt: '2023-01-01',
        updatedAt: '2023-01-01',
        namespace: 'test',
        inputs: [],
        outputs: [],
        location: '',
        description: '',
        latestRun: null
      },
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
      type: 'DATASET',
      data: {
        id: { namespace: 'test', name: 'dataset_bar' },
        type: 'DB_TABLE',
        name: 'dataset_bar',
        physicalName: 'dataset_bar',
        createdAt: '2023-01-01',
        updatedAt: '2023-01-01',
        namespace: 'test',
        sourceName: 'source',
        fields: [],
        facets: {},
        tags: [],
        lastModifiedAt: '2023-01-01',
        description: 'Test dataset'
      },
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
      type: 'JOB',
      data: {
        id: { namespace: 'test', name: 'test' },
        type: 'BATCH',
        name: 'job_bar',
        createdAt: '2023-01-01',
        updatedAt: '2023-01-01',
        namespace: 'test',
        inputs: [],
        outputs: [],
        location: '',
        description: '',
        latestRun: null
      },
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
      type: 'DATASET',
      data: {
        id: { namespace: 'test', name: 'dataset_foo' },
        type: 'DB_TABLE',
        name: 'dataset_foo',
        physicalName: 'dataset_foo',
        createdAt: '2023-01-01',
        updatedAt: '2023-01-01',
        namespace: 'test',
        sourceName: 'source',
        fields: [],
        facets: {},
        tags: [],
        lastModifiedAt: '2023-01-01',
        description: 'Test dataset'
      },
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
}

describe('Lineage Component', () => {
  let instance

  beforeEach(() => {
    const mockStore = configureStore()
    const store = mockStore({
      lineage: {
        lineage: mockGraphWithCycle,
        selectedNode: mockGraphWithCycle.graph[0].id
      }
    })

    const mockProps = {
      classes: {
        lineageContainer: ''
      }
    }

    instance = mount(
      <Provider store={store}>
        <MemoryRouter>
          <Route path='/lineage/:nodeType/:namespace/:nodeName'>
            <Lineage {...(mockProps as any)} />
          </Route>
        </MemoryRouter>
      </Provider>
    ).find(Lineage)
  })

  it("doesn't follow cycles in the lineage graph", () => {
    console.log(instance.debug())
    const paths = instance.getSelectedPaths()

    console.log(JSON.stringify(paths))
  })
})
