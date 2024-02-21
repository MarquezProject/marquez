import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { LineageDataset } from '../../components/lineage/types'
import { LineageGraph } from '../../types/api'
import { PositionedNode } from '../../../libs/graph'
import { THEME_EXTRA, theme } from '../../helpers/theme'
import { TableLineageDatasetNodeData } from './nodes'
import { connect } from 'react-redux'
import { faDatabase } from '@fortawesome/free-solid-svg-icons/faDatabase'
import { grey } from '@mui/material/colors'
import { truncateText } from '../../helpers/text'
import { useNavigate, useParams } from 'react-router-dom'
import Box from '@mui/system/Box'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
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

  const navigate = useNavigate()
  const { name, namespace } = useParams()
  const isSelected = name === node.data.dataset.name && namespace === node.data.dataset.namespace

  const handleClick = () => {
    navigate(
      `/lineage/dataset/${encodeURIComponent(node.data.dataset.namespace)}/${encodeURIComponent(
        node.data.dataset.name
      )}?tableLevelNode=${encodeURIComponent(node.id)}`
    )
  }

  const addToToolTip = (dataset: LineageDataset) => {
    return (
      <>
        <b>{'Namespace: '}</b>
        {dataset.namespace}
        <br></br>
        <b>{'Name: '}</b>
        {dataset.name}
        <br></br>
        <b>{'Description: '}</b>
        {dataset.description === null ? 'No Description' : dataset.description}
        <br></br>
      </>
    )
  }

  return (
    <MQTooltip title={addToToolTip(node.data.dataset)}>
      <g>
        <Box
          component={'rect'}
          sx={{
            x: 0,
            y: 0,
            width: node.width,
            height: node.height,
            stroke: isSelected ? theme.palette.primary.main : grey['100'],
            filter: isSelected ? `drop-shadow( 0 0 4px ${theme.palette.primary.main})` : 'none',
            rx: 4,
            fill: theme.palette.background.paper,
            cursor: 'pointer',
            transition: 'filter 0.3',
          }}
          cursor={'pointer'}
          onClick={handleClick}
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
          cursor={'pointer'}
          onClick={handleClick}
        />
        <text fontSize='8' fill={'white'} x={20} y={14} cursor={'pointer'} onClick={handleClick}>
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
                - {truncateText(field.name, 15)}
              </text>
            )
          })}
      </g>
    </MQTooltip>
  )
}

TableLineageDatasetNode.getLayoutOptions = (node: TableLineageDatasetNodeProps['node']) => ({
  ...node,
})

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
})

export default connect(mapStateToProps)(TableLineageDatasetNode)
