import React, { useMemo } from 'react'

import { chakra, keyframes, usePrefersReducedMotion } from '@chakra-ui/react'

import { EdgeLabel } from './EdgeLabel'
import { grey } from '@mui/material/colors'
import type { EdgeProps } from './Edge'

const ChakraPolyline = chakra('polyline') // need to use animation prop
const marchingAnts = keyframes({ from: { strokeDashoffset: 60 }, to: { strokeDashoffset: 0 } })

export const ElbowEdge = ({ edge, isMiniMap }: EdgeProps) => {
  const reducedMotion = usePrefersReducedMotion() || isMiniMap // do not animate the minimap

  const points = useMemo(() => {
    const { startPoint, bendPoints, endPoint } = edge

    return [
      // source
      { x: startPoint.x, y: startPoint.y },
      ...(bendPoints?.map((bendPoint) => ({ x: bendPoint.x, y: bendPoint.y })) ?? []),
      // target
      { x: endPoint.x, y: endPoint.y },
    ]
  }, [edge])

  // Find the longest edge that the label would be near
  let longestEdge: { y: number; length: number } | undefined
  if (edge.label) {
    points.forEach((p, i) => {
      if (i > 0) {
        const length = p.x - points[i - 1].x
        if (!longestEdge || longestEdge.length < length) longestEdge = { y: p.y, length }
      }
    })
  }
  return (
    <>
      <polyline
        id={`${edge.sourceNodeId}-${edge.targetNodeId}`}
        fill='none'
        stroke={edge.color || grey['600']}
        strokeWidth={edge.strokeWidth || 2}
        strokeLinejoin='round'
        points={points.map(({ x, y }) => `${x},${y}`).join(' ')}
      />
      <EdgeLabel label={edge.label} endPointY={longestEdge?.y} />
      {!reducedMotion && edge.isAnimated && (
        <ChakraPolyline
          id={`${edge.sourceNodeId}-${edge.targetNodeId}-animated`}
          fill='none'
          strokeLinecap='round'
          stroke={edge.color || grey['600']}
          strokeWidth={edge.strokeWidth || 5}
          strokeLinejoin='round'
          strokeDasharray='0px 60px'
          animation={`${marchingAnts} infinite 2s linear`}
          points={points.map(({ x, y }) => `${x},${y}`).join(' ')}
        />
      )}
    </>
  )
}
