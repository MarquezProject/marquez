import { ColumnLineageColumnNodeData, ColumnLineageDatasetNodeData } from './nodes'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { PositionedNode } from '../../../libs/graph'
import { faDatabase } from '@fortawesome/free-solid-svg-icons'
import { grey } from '@mui/material/colors'
import { theme } from '../../helpers/theme'
import Box from '@mui/system/Box'
import React from 'react'

interface ColumnLineageDatasetNodeProps {
  node: PositionedNode<'dataset', ColumnLineageDatasetNodeData>
}
export const ColumnLineageDatasetNode = ({ node }: ColumnLineageDatasetNodeProps) => (
  <>
    <Box
      component={'rect'}
      sx={{
        x: 0,
        y: 0,
        width: node.width,
        height: node.height,
        fill: theme.palette.background.paper,
        strokeWidth: 1,
        rx: 4,
      }}
    />
    <text x={16} y={24} textAnchor='top' fontSize={14} stroke={'white'}>
      {`${node.data.dataset}`}
    </text>
    <FontAwesomeIcon
      x={node.width - 24}
      y={12}
      width={16}
      height={16}
      color={theme.palette.primary.main}
      icon={faDatabase}
    />
  </>
)

ColumnLineageDatasetNode.getLayoutOptions = (node: ColumnLineageDatasetNodeProps['node']) => {
  return {
    ...node,
    padding: { left: 40, top: 40, right: 40, bottom: 16 },
  }
}

interface ColumnLineageColumnNodeProps {
  node: PositionedNode<'column', ColumnLineageColumnNodeData>
}
export const ColumnLineageColumnNode = ({ node }: ColumnLineageColumnNodeProps) => (
  <>
    <Box
      component={'rect'}
      sx={{
        x: 0,
        y: 0,
        width: node.width,
        height: node.height,
        stroke: grey['100'],
        rx: 4,
        fill: grey['900'],
      }}
    />
    <text x={8} y={16} textAnchor='top' fontSize={12} stroke={grey[400]}>
      {node.data.column}
    </text>
  </>
)

ColumnLineageColumnNode.getLayoutOptions = (node: ColumnLineageColumnNodeProps['node']) => ({
  ...node,
})
