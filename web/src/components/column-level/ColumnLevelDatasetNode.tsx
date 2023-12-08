import { Box } from '@mui/material'
import { NodeRendererMap, PositionedNode } from '../../../libs/graph'
import { grey } from '@mui/material/colors'
import React from 'react'

export interface ColumnLevelDatasetData {
  name?: string
}

interface ColumnLevelDatasetProps {
  node: PositionedNode<'simple', ColumnLevelDatasetData>
}
const ColumnLevelDataset = ({ node }: ColumnLevelDatasetProps) => (
  <>
    <Box
      component={'rect'}
      sx={{
        x: 0,
        y: 0,
        width: node.width,
        height: node.height,
        fill: 'none',
        stroke: grey['500'],
        strokeWidth: 1,
      }}
    />
    <text x={10} y={20} textAnchor='top' fontSize={12}>
      {node.data.name || node.id}
    </text>
  </>
)

// Node renders may include a function to add or modify node properties.
ColumnLevelDataset.getLayoutOptions = (node: any) =>
  node.children && node.children.length > 0
    ? {
        ...node,
        // padding around children
        padding: { left: 10, top: 10, right: 10, bottom: 10 },
      }
    : {
        ...node,
        // Size with no children
        width: 100,
        height: 100,
      }

export const customNodesRenderers: NodeRendererMap<'simple', ColumnLevelDatasetData> =
  new Map().set('simple', ColumnLevelDataset)
