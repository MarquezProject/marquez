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
import { faCaretRight } from '@fortawesome/free-solid-svg-icons/faCaretRight'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { faDatabase } from '@fortawesome/free-solid-svg-icons/faDatabase'
import { setSelectedNode } from '../../../../store/actionCreators'
import { theme } from '../../../../helpers/theme'

export type Vertex = {
  x: number
  y: number
}

const RADIUS = 14
const OUTER_RADIUS = RADIUS + 8
const ICON_SIZE = 16
const BORDER = 2

interface DispatchProps {
  setSelectedNode: (payload: string) => void
}

interface OwnProps {
  node: GraphNode<MqNode>
  edgeEnds: Vertex[]
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
    const { node, edgeEnds, selectedNode } = this.props
    const job = isJob(node)
    return (
      <Link
        to={this.determineLink(node)}
        onClick={() => node.label && this.props.setSelectedNode(node.label)}
      >
        {job ? (
          <g>
            <circle
              style={{ cursor: 'pointer' }}
              r={RADIUS}
              fill={theme.palette.common.white}
              stroke={
                selectedNode === node.label
                  ? theme.palette.primary.main
                  : theme.palette.secondary.main
              }
              strokeWidth={BORDER}
              cx={node.x}
              cy={node.y}
            />
            <circle
              style={{ cursor: 'pointer' }}
              r={RADIUS - 2}
              fill={theme.palette.common.white}
              stroke={theme.palette.common.white}
              strokeWidth={2}
              cx={node.x}
              cy={node.y}
            />
            <FontAwesomeIcon
              style={{ transformOrigin: `${node.x}px ${node.y}px` }}
              icon={faCog}
              width={ICON_SIZE}
              height={ICON_SIZE}
              x={node.x - ICON_SIZE / 2}
              y={node.y - ICON_SIZE / 2}
              color={
                selectedNode === node.label
                  ? theme.palette.primary.main
                  : theme.palette.secondary.main
              }
            />
          </g>
        ) : (
          <g>
            <rect
              style={{ cursor: 'pointer' }}
              x={node.x - RADIUS}
              y={node.y - RADIUS}
              fill={theme.palette.common.white}
              stroke={
                selectedNode === node.label
                  ? theme.palette.primary.main
                  : theme.palette.secondary.main
              }
              strokeWidth={BORDER}
              width={RADIUS * 2}
              height={RADIUS * 2}
              rx={4}
            />
            <rect
              style={{ cursor: 'pointer' }}
              x={node.x - (RADIUS - 2)}
              y={node.y - (RADIUS - 2)}
              fill={theme.palette.common.white}
              stroke={theme.palette.common.white}
              strokeWidth={BORDER}
              width={(RADIUS - 2) * 2}
              height={(RADIUS - 2) * 2}
              rx={4}
            />
            <FontAwesomeIcon
              icon={faDatabase}
              width={ICON_SIZE}
              height={ICON_SIZE}
              x={node.x - ICON_SIZE / 2}
              y={node.y - ICON_SIZE / 2}
              color={
                selectedNode === node.label
                  ? theme.palette.primary.main
                  : theme.palette.secondary.main
              }
            />
          </g>
        )}
        {edgeEnds.find(edge => edge.x === node.x && edge.y === node.y) && (
          <FontAwesomeIcon
            icon={faCaretRight}
            x={node.x - OUTER_RADIUS - ICON_SIZE / 2}
            y={node.y - ICON_SIZE / 2}
            width={ICON_SIZE}
            height={ICON_SIZE}
            color={theme.palette.secondary.main}
          />
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

export default connect(
  null,
  mapDispatchToProps
)(Node)
