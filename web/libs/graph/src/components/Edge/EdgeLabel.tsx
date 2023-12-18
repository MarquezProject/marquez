import React from 'react'

import { grey } from '@mui/material/colors'
import type { ElkLabel } from 'elkjs'

interface Props {
  label?: ElkLabel
  endPointY?: number
}

export const EdgeLabel = ({ label, endPointY }: Props) => {
  const labelColor = grey['400']

  if (!label || !label.y || !label.x) return null

  let { y } = label
  // The edge and label are rendering a little differently,
  // so we need some extra magic numbers to work right
  if (endPointY) y = label.y - 5 >= endPointY ? endPointY + 25 : endPointY - 15

  return (
    <text fill={labelColor} x={label.x} y={y}>
      {label.text}
    </text>
  )
}
