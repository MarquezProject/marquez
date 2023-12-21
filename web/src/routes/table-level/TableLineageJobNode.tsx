import { IState } from '../../store/reducers'
import { LineageGraph } from '../../types/api'
import { PositionedNode } from '../../../libs/graph'
import { TableLineageJobNodeData } from './nodes'
import { connect } from 'react-redux'
import { grey } from '@mui/material/colors'
import Box from '@mui/system/Box'
import React from 'react'

interface StateProps {
  lineage: LineageGraph
}

interface TableLineageJobNodeProps {
  node: PositionedNode<'job', TableLineageJobNodeData>
}

const TableLineageJobNode = ({ node }: TableLineageJobNodeProps & StateProps) => {
  return (
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
          cursor: 'pointer',
          transition: 'filter 0.3',
        }}
      />
    </>
  )
}

TableLineageJobNode.getLayoutOptions = (node: TableLineageJobNodeProps['node']) => ({
  ...node,
})

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
})

export default connect(mapStateToProps)(TableLineageJobNode)
