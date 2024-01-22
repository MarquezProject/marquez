import { ColumnLineageColumnNodeData } from './nodes'
import { ColumnLineageGraph } from '../../types/api'
import { IState } from '../../store/reducers'
import { PositionedNode } from '../../../libs/graph'
import { connect } from 'react-redux'
import { grey } from '@mui/material/colors'
import { truncateText } from '../../helpers/text'
import { useSearchParams } from 'react-router-dom'
import Box from '@mui/system/Box'
import React from 'react'

interface StateProps {
  columnLineage: ColumnLineageGraph
}

interface ColumnLineageColumnNodeProps {
  node: PositionedNode<'column', ColumnLineageColumnNodeData>
}

export const encodeQueryString = (namespace: string, dataset: string, column: string) => {
  return `datasetField:${namespace}:${dataset}:${column}`
}

const ColumnLineageColumnNode = ({ node }: ColumnLineageColumnNodeProps & StateProps) => {
  const [searchParams, setSearchParams] = useSearchParams()
  const [shine, setShine] = React.useState(false)
  return (
    <>
      <Box
        onMouseEnter={() => {
          setShine(true)
          setSearchParams({
            ...searchParams,
            column: encodeQueryString(node.data.namespace, node.data.dataset, node.data.column),
          })
        }}
        onMouseLeave={() => {
          setShine(false)
        }}
        onClick={() => {
          setSearchParams({
            ...searchParams,
            dataset: node.data.dataset,
            namespace: node.data.namespace,
            column: encodeQueryString(node.data.namespace, node.data.dataset, node.data.column),
          })
        }}
        component={'rect'}
        sx={{
          x: 0,
          y: 0,
          width: node.width,
          height: node.height,
          stroke: grey['100'],
          rx: 4,
          fill: grey['900'],
          cursor: 'pointer',
          filter: shine ? 'drop-shadow( 0 0 4px white)' : 'none',
          transition: 'filter 0.3',
        }}
      />
      <text
        onMouseEnter={() => {
          setShine(true)
          setSearchParams({
            ...searchParams,
            column: encodeQueryString(node.data.namespace, node.data.dataset, node.data.column),
          })
        }}
        onMouseLeave={() => {
          setShine(false)
        }}
        onClick={() => {
          setSearchParams({
            ...searchParams,
            dataset: node.data.dataset,
            namespace: node.data.namespace,
            column: encodeQueryString(node.data.namespace, node.data.dataset, node.data.column),
          })
        }}
        x={8}
        y={16}
        textAnchor='top'
        fontSize={12}
        cursor={'pointer'}
        stroke={grey[400]}
      >
        {truncateText(node.data.column, 25)}
      </text>
    </>
  )
}

ColumnLineageColumnNode.getLayoutOptions = (node: ColumnLineageColumnNodeProps['node']) => ({
  ...node,
})

const mapStateToProps = (state: IState) => ({
  columnLineage: state.columnLineage.columnLineage,
})

export default connect(mapStateToProps)(ColumnLineageColumnNode)
