// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { GraphEdge } from 'dagre'
import { LinePath } from '@visx/shape'
import { curveMonotoneX } from '@visx/curve'
import { faCaretRight } from '@fortawesome/free-solid-svg-icons/faCaretRight'
import { theme } from '../../../../helpers/theme'
import React from 'react'

type EdgeProps = {
  edgePoints: GraphEdge[]
}

type EdgePoint = {
  isSelected: boolean
  points: {
    x: number
    y: number
  }[]
}

const RADIUS = 14
const OUTER_RADIUS = RADIUS + 8
const ICON_SIZE = 16

class Edge extends React.Component<EdgeProps> {
  getPoints = (edge: EdgePoint) => edge.points[edge.points.length - 1]

  render() {
    const { edgePoints } = this.props
    const edgeEnds = edgePoints.map(edge => {
      const isSelected = edgePoints.find(
        o =>
          this.getPoints(o as EdgePoint).x == this.getPoints(edge as EdgePoint).x &&
          this.getPoints(o as EdgePoint).y == this.getPoints(edge as EdgePoint).y &&
          o.isSelected === true
      )
      return {
        ...edge.points[edge.points.length - 1],
        ...{ isSelected: typeof isSelected !== 'undefined' }
      }
    })

    return (
      <>
        {edgePoints.map((edge, i) => (
          <LinePath<{ x: number; y: number }>
            key={i}
            curve={curveMonotoneX}
            data={edge.points}
            x={(d, index) => (index === 0 ? d.x + 20 : d.x - 25)}
            y={d => d.y}
            stroke={edge.isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
            strokeWidth={1}
            opacity={1}
            shapeRendering='geometricPrecision'
          />
        ))}
        {edgeEnds.map((edge, i) => (
          <FontAwesomeIcon
            key={i}
            icon={faCaretRight}
            x={edge.x - OUTER_RADIUS - ICON_SIZE / 2}
            y={edge.y - ICON_SIZE / 2}
            width={ICON_SIZE}
            height={ICON_SIZE}
            color={edge.isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
          />
        ))}
      </>
    )
  }
}

export default Edge
