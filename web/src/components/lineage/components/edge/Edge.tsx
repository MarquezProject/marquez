// SPDX-License-Identifier: Apache-2.0

import { GraphEdge } from 'dagre'
import { LinePath } from '@visx/shape'
import { curveMonotoneX } from '@visx/curve'
import { theme } from '../../../../helpers/theme'
import React from 'react'

type EdgeProps = {
  edgePoints: GraphEdge[]
}

class Edge extends React.Component<EdgeProps> {
  render() {
    const { edgePoints } = this.props
    return (
      <>
        {edgePoints.map((edge, i) => (
          <LinePath<{ x: number; y: number }>
            key={i}
            curve={curveMonotoneX}
            data={edge.points}
            x={(d, index) => (index === 0 ? d.x + 20 : d.x - 25)}
            y={d => d.y}
            stroke={theme.palette.secondary.main}
            strokeWidth={1}
            opacity={1}
            shapeRendering='geometricPrecision'
          />
        ))}
      </>
    )
  }
}

export default Edge
