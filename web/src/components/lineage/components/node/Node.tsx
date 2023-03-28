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

class Node extends React.Component<NodeProps> {
  determineLink = (node: GraphNode<MqNode>) => {
    if (isJob(node)) {
      return `/lineage/${encodeNode('JOB', node.data.namespace, node.data.name)}`
    } else if (isDataset(node)) {
      return `/lineage/${encodeNode('DATASET', node.data.namespace, node.data.name)}`
    }
    return '/'
  }

  render() {
    const { node, selectedNode } = this.props
    const job = isJob(node)
    const isSelected = selectedNode === node.label
    const ariaJobLabel = 'Job'
    const ariaDatasetLabel = 'Dataset'
    return (
      <Link
        to={this.determineLink(node)}
        onClick={() => node.label && this.props.setSelectedNode(node.label)}
      >
        {job ? (
          <g>
            {/* { console.log(job.latestRun)} */}
            {/* {console.log(runStateToNodeColor(job.latestRun))} */}
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
              title={ariaJobLabel}
              aria-hidden={'true'}
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
              stroke={isSelected ? theme.palette.primary.main: theme.palette.secondary.main}
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
              title={ariaDatasetLabel}
              aria-hidden={'true'}
              icon={faDatabase}
              width={ICON_SIZE}
              height={ICON_SIZE}
              x={node.x - ICON_SIZE / 2}
              y={node.y - ICON_SIZE / 2}
              color={isSelected ? theme.palette.primary.main : theme.palette.secondary.main}
            />
          </g>
        )}
        <NodeText node={node} />
      </Link>
    )
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(Node)
