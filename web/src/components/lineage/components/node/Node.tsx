// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import * as Redux from 'redux'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Node as GraphNode } from 'dagre'
import { Link } from 'react-router-dom'
import { MqNode } from '../../types'
import { NodeText } from './NodeText'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode, isDataset, isJob } from '../../../../helpers/nodes'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { faDatabase } from '@fortawesome/free-solid-svg-icons/faDatabase'
import { setSelectedNode } from '../../../../store/actionCreators'
import { theme } from '../../../../helpers/theme'
import MQTooltip from '../../../core/tooltip/MQTooltip'

const RADIUS = 14
const ICON_SIZE = 16
const BORDER = 4

interface DispatchProps {
  setSelectedNode: (payload: string) => void
}

interface OwnProps {
  node: GraphNode<MqNode>
  selectedNode: string
}

type NodeProps = DispatchProps & OwnProps

export const determineLink = (node: GraphNode<MqNode>) => {
  if (isJob(node)) {
    return `/lineage/${encodeNode('JOB', node.data.namespace, node.data.name)}`
  } else if (isDataset(node)) {
    return `/lineage/${encodeNode('DATASET', node.data.namespace, node.data.name)}`
  }
  return '/'
}

const Node: React.FC<NodeProps> = ({ node, selectedNode, setSelectedNode }) => {
  const addToToolTip = (inputData: GraphNode<MqNode>) => {
    return (
      <>
        <b>{'Namespace: '}</b>
        {inputData.data.namespace}
        <br></br>
        <b>{'Name: '}</b>
        {inputData.data.name}
        <br></br>
        <b>{'Description: '}</b>
        {inputData.data.description === null ? 'No Description' : inputData.data.description}
        <br></br>
      </>
    )
  }

  const job = isJob(node)
  const isSelected = selectedNode === node.label
  const ariaJobLabel = 'Job'
  const ariaDatasetLabel = 'Dataset'

  return (
    <Link to={determineLink(node)} onClick={() => node.label && setSelectedNode(node.label)}>
      <MQTooltip title={addToToolTip(node)}>
        {job ? (
          <g>
            <circle
              style={{ cursor: 'pointer' }}
              r={RADIUS}
              fill={isSelected ? theme.palette.secondary.main : theme.palette.common.white}
              stroke={isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
              strokeWidth={BORDER / 2}
              cx={node.x}
              cy={node.y}
            />
            <FontAwesomeIcon
              aria-hidden={'true'}
              title={ariaJobLabel}
              style={{ transformOrigin: `${node.x}px ${node.y}px` }}
              icon={faCog}
              width={ICON_SIZE}
              height={ICON_SIZE}
              x={node.x - ICON_SIZE / 2}
              y={node.y - ICON_SIZE / 2}
              color={isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
            />
          </g>
        ) : (
          <g>
            <rect
              style={{ cursor: 'pointer' }}
              x={node.x - RADIUS}
              y={node.y - RADIUS}
              fill={isSelected ? theme.palette.secondary.main : theme.palette.common.white}
              stroke={isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
              strokeWidth={BORDER / 2}
              width={RADIUS * 2}
              height={RADIUS * 2}
              rx={4}
            />
            <rect
              style={{ cursor: 'pointer' }}
              x={node.x - (RADIUS - 2)}
              y={node.y - (RADIUS - 2)}
              fill={isSelected ? theme.palette.secondary.main : theme.palette.common.white}
              width={(RADIUS - 2) * 2}
              height={(RADIUS - 2) * 2}
              rx={4}
            />
            <FontAwesomeIcon
              aria-hidden={'true'}
              title={ariaDatasetLabel}
              icon={faDatabase}
              width={ICON_SIZE}
              height={ICON_SIZE}
              x={node.x - ICON_SIZE / 2}
              y={node.y - ICON_SIZE / 2}
              color={isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
            />
          </g>
        )}
      </MQTooltip>
      <NodeText node={node} />
    </Link>
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode,
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(Node)
