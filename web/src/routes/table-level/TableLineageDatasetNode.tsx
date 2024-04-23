import { ChevronLeft } from '@mui/icons-material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { LineageDataset } from '../../types/lineage'
import { LineageGraph } from '../../types/api'
import { PositionedNode } from '../../../libs/graph'
import { THEME_EXTRA, theme } from '../../helpers/theme'
import { TableLineageDatasetNodeData } from './nodes'
import { connect } from 'react-redux'

import { faDatabase } from '@fortawesome/free-solid-svg-icons/faDatabase'
import { truncateText } from '../../helpers/text'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Box from '@mui/system/Box'
import IconButton from '@mui/material/IconButton'
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
  const [searchParams, setSearchParams] = useSearchParams()
  const isCollapsed = searchParams.get('collapsedNodes')?.split(',').includes(node.id)

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
    <g>
      <Box
        component={'rect'}
        sx={{
          x: 0,
          y: 0,
          width: node.width,
          height: node.height,
          filter: isSelected ? `drop-shadow( 0 0 4px ${theme.palette.primary.main})` : 'none',
          rx: 4,
          fill: theme.palette.background.paper,
          cursor: 'pointer',
          transition: 'all 0.3',
        }}
        cursor={'pointer'}
        onClick={handleClick}
      />
      <Box
        component={'rect'}
        x={0}
        y={0}
        height={24}
        width={24}
        sx={{ rx: 4, fill: theme.palette.info.main }}
      />

      <FontAwesomeIcon
        aria-hidden={'true'}
        title={'Job'}
        icon={faDatabase}
        width={ICON_SIZE}
        height={ICON_SIZE}
        x={6}
        y={ICON_SIZE / 2}
        color={theme.palette.common.white}
        onClick={handleClick}
      />
      <foreignObject width={16} height={24} x={node.width - 18} y={0}>
        <MQTooltip title={isCollapsed ? 'Expand' : 'Collapse'} placement={'top'}>
          <IconButton
            sx={{ width: 10, height: 10 }}
            onClick={(event) => {
              event.stopPropagation()
              const collapsedNodes = searchParams.get('collapsedNodes')
              if (collapsedNodes) {
                const collapsedNodesArray = collapsedNodes.split(',')
                if (collapsedNodesArray.includes(node.id)) {
                  collapsedNodesArray.splice(collapsedNodesArray.indexOf(node.id), 1)
                } else {
                  collapsedNodesArray.push(node.id)
                }
                searchParams.set('collapsedNodes', collapsedNodesArray.toString())
              } else {
                searchParams.set('collapsedNodes', node.id)
              }
              setSearchParams(searchParams)
            }}
          >
            <ChevronLeft
              sx={{
                width: 10,
                height: 10,
                rotate: !isCollapsed ? '-90deg' : 0,
                transition: 'rotate .3s',
              }}
            />
          </IconButton>
        </MQTooltip>
      </foreignObject>
      <MQTooltip title={addToToolTip(node.data.dataset)}>
        <g>
          <text
            fontSize='8'
            fontFamily={`${'Source Code Pro'}, mono`}
            fill={'white'}
            x={28}
            y={10}
            onClick={handleClick}
            cursor={'pointer'}
          >
            DATASET
          </text>
          <text fontSize='8' fill={'white'} x={28} y={20} cursor={'pointer'} onClick={handleClick}>
            {truncateText(node.data.dataset.name, 15)}
          </text>
        </g>
      </MQTooltip>

      {!isCompact &&
        node.data.dataset.fields.map((field, index) => {
          return (
            <text
              key={field.name}
              fontSize='8'
              fill={THEME_EXTRA.typography.subdued}
              x={10}
              y={14 + 10 + 10 * (index + 1)}
            >
              - {truncateText(field.name, 20)}
            </text>
          )
        })}
    </g>
  )
}

TableLineageDatasetNode.getLayoutOptions = (node: TableLineageDatasetNodeProps['node']) => ({
  ...node,
})

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
})

export default connect(mapStateToProps)(TableLineageDatasetNode)
