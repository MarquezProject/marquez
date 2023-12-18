import React from 'react'

import { grey } from '@mui/material/colors'

import { centerItemInContainer } from '../../utils/d3'

interface Props {
  k: number
  contentWidth: number
  contentHeight: number
  hideDotGrid?: boolean
  backgroundColor?: string
  dotGridColor?: string
}

const GAP = 12 // Gap between background dots
const THRESHOLD_GAP = 16 // Minimum size
const SIZE = 0.5 // Size of the background dots

const findMinGap = (gap: number, k: number): number =>
  k * gap > THRESHOLD_GAP ? gap : findMinGap(gap * 2, k)

export const Background = ({
  k,
  contentWidth,
  contentHeight,
  hideDotGrid = false,
  backgroundColor,
  dotGridColor,
}: Props) => {
  const scaledGap = findMinGap(GAP, k)

  const bgColor = grey['800']
  const dotColor = grey['400']

  // Use maxDimension to ensure converge when there are narrow contents in square containers.
  const maxDimension = Math.max(contentWidth, contentHeight)
  const maxDimensionHalf = maxDimension / 2
  const centerTransform = centerItemInContainer(
    1,
    [
      [maxDimensionHalf, maxDimensionHalf],
      [maxDimensionHalf, maxDimensionHalf],
    ],
    [
      [0, 0],
      [contentWidth, contentHeight],
    ]
  )

  return (
    <>
      <pattern
        id='lineage-graph-dots'
        x={0}
        y={0}
        width={scaledGap}
        height={scaledGap}
        patternUnits='userSpaceOnUse'
      >
        <rect x={0} y={0} width={scaledGap} height={scaledGap} fill={backgroundColor || bgColor} />
        {!hideDotGrid && (
          <circle cx={SIZE / k} cy={SIZE / k} r={SIZE / k} fill={dotGridColor || dotColor} />
        )}
      </pattern>
      <rect
        transform={centerTransform.toString()}
        // extend beyond the edges of the svg to allow for overscroll
        x={-maxDimension * 2}
        y={-maxDimension * 2}
        width={maxDimension * 5}
        height={maxDimension * 5}
        fill='url(#lineage-graph-dots)'
      />
    </>
  )
}
