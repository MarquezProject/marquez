// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { shallow } from 'enzyme'
import Lineage from '../../components/lineage/Lineage'

const mockGraphWithCycle = [
  {
    id: 'job_foo',
    type: 'JOB',
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
  const mockProps = {}
  it("doesn't follow cycles in the lineage graph", () => {
    const wrapper = shallow(
      <Lineage
        {...mockProps}
        // TODO: fix type errors, probably needs a stub interface?
        lineage={mockGraphWithCycle}
        selectedNode={mockGraphWithCycle[0].id}
      />
    )

    const instance = wrapper.instance()
    // TODO: fix type errors, the above stub interface will probably fix this
    instance.getSelectedPaths()
  })
})
