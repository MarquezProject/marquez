import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { LineageGraph } from '../../types/api'
import { PositionedNode } from '../../../libs/graph'
import { TableLineageJobNodeData } from './nodes'
import { connect } from 'react-redux'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { grey } from '@mui/material/colors'
import { theme } from '../../helpers/theme'
import { truncateText } from '../../helpers/text'
import Box from '@mui/system/Box'
import React from 'react'

interface StateProps {
  lineage: LineageGraph
}

interface TableLineageJobNodeProps {
  node: PositionedNode<'job', TableLineageJobNodeData>
}

const ICON_SIZE = 12

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
      <FontAwesomeIcon
        aria-hidden={'true'}
        title={'Job'}
        icon={faCog}
        width={ICON_SIZE}
        height={ICON_SIZE}
        x={4}
        y={ICON_SIZE / 2}
        color={theme.palette.primary.main}
      />
      <text fontSize='8' fill={'white'} x={20} y={14}>
        {truncateText(node.data.job.name, 15)}
      </text>
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
