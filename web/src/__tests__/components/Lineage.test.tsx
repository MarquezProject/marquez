// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Lineage, LineageProps } from '../../components/lineage/Lineage'
import { LineageNode } from '../../components/lineage/types'
import { LineageGraph } from '../../types/api'

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

function mockSetState(newState: any) {
    this.state = {...this.state, ...newState}
}

describe('Lineage Component', () => {
 let instance: Lineage

  beforeEach(() => {
      instance = new Lineage({selectedNode: "job_foo"} as unknown as LineageProps)
      instance.setState = mockSetState
      instance.initGraph()
      instance.buildGraphAll(mockGraphWithCycle as unknown as LineageNode[])
  })

  it("doesn't follow cycles in the lineage graph", () => {
    const paths = instance.getSelectedPaths()

      const pathCounts = paths.reduce((acc, p) => {
        const pathId = p.join(":")
          acc[pathId] = acc[pathId] ? 1 : acc[pathId];
          return acc;
      }, {} as Record<string, number>)

      expect(Object.values(pathCounts).some(c => c > 2)).toBe(false);
  })
})
