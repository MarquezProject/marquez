import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { LineageGraph } from '../../types/api'
import { PositionedNode } from '../../../libs/graph'
import { THEME_EXTRA, theme } from '../../helpers/theme'
import { TableLineageDatasetNodeData } from './nodes'
import { connect } from 'react-redux'
import { faDatabase } from '@fortawesome/free-solid-svg-icons/faDatabase'
import { grey } from '@mui/material/colors'
import { truncateText } from '../../helpers/text'
import Box from '@mui/system/Box'
import React from 'react'

interface StateProps {
  lineage: LineageGraph
}

interface TableLineageDatasetNodeProps {
  node: PositionedNode<'DATASET', TableLineageDatasetNodeData>
}

const ICON_SIZE = 12
const COMPACT_HEIGHT = 24

const TableLineageDatasetNode = ({ node }: TableLineageDatasetNodeProps & StateProps) => {
  const isCompact = node.height === COMPACT_HEIGHT
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
        icon={faDatabase}
        width={ICON_SIZE}
        height={ICON_SIZE}
        x={4}
        y={ICON_SIZE / 2}
        color={theme.palette.warning.main}
      />
      <text fontSize='8' fill={'white'} x={20} y={14}>
        {truncateText(node.data.dataset.name, 15)}
      </text>
      {!isCompact &&
        node.data.dataset.fields.map((field, index) => {
          return (
            <text
              key={field.name}
              fontSize='8'
              fill={THEME_EXTRA.typography.subdued}
              x={20}
              y={14 + 10 + 10 * (index + 1)}
            >
              {truncateText(field.name, 15)}
            </text>
          )
        })}
    </>
  )
}

TableLineageDatasetNode.getLayoutOptions = (node: TableLineageDatasetNodeProps['node']) => ({
  ...node,
})

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
})

export default connect(mapStateToProps)(TableLineageDatasetNode)
