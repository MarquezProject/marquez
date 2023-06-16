// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import '../../i18n/config'
import * as Redux from 'redux'
import { Box } from '@mui/material'
import { DAGRE_CONFIG, INITIAL_TRANSFORM, NODE_SIZE } from './config'
import { GraphEdge, Node as GraphNode, graphlib, layout } from 'dagre'
import { HEADER_HEIGHT } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { JobOrDataset, LineageNode, MqNode } from './types'
import { LineageGraph } from '../../types/api'
import { Zoom } from '@visx/zoom'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchLineage, resetLineage, setSelectedNode } from '../../store/actionCreators'
import { generateNodeId } from '../../helpers/nodes'
import { localPoint } from '@visx/event'
import { useParams } from 'react-router-dom'
import Edge from './components/edge/Edge'
import MqEmpty from '../core/empty/MqEmpty'
import MqText from '../core/text/MqText'
import Node from './components/node/Node'
import ParentSize from '@visx/responsive/lib/components/ParentSize'

const BOTTOM_OFFSET = 8
const MIN_ZOOM = 1 / 4
const MAX_ZOOM = 4
const DOUBLE_CLICK_MAGNIFICATION = 1.1

interface StateProps {
  lineage: LineageGraph
  selectedNode: string
}

interface LineageState {
  graph: graphlib.Graph<MqNode>
  edges: GraphEdge[]
  nodes: GraphNode<MqNode>[]
}

interface DispatchProps {
  setSelectedNode: typeof setSelectedNode
  fetchLineage: typeof fetchLineage
  resetLineage: typeof resetLineage
}

export interface JobOrDatasetMatchParams {
  nodeName: string
  namespace: string
  nodeType: string
}

type LineageProps = StateProps &
  DispatchProps 

let g: graphlib.Graph<MqNode>

const Lineage: React.FC<LineageProps> = (props: LineageProps) => {
  const [state, setState] = React.useState<LineageState>({
    graph: g,
    edges: [],
    nodes: []
  })
  const { nodeName, namespace, nodeType } = useParams()
  const mounted = React.useRef<boolean>(false)

  const prevLineage = React.useRef<LineageGraph>()
  const prevSelectedNode = React.useRef<string>()

  React.useEffect(() => {
    if (!mounted.current) {
      console.log('mount TODO remove')
      // on mount
      if (nodeName && namespace && nodeType) {
        const nodeId = generateNodeId(
          nodeType.toUpperCase() as JobOrDataset,
          namespace,
          nodeName
        )
        props.setSelectedNode(nodeId)
        props.fetchLineage(
          nodeType.toUpperCase() as JobOrDataset,
          namespace,
          nodeName
        )
      }
      mounted.current = true
    } else {
      // on update
      // TODO BUG HERE
      if (
        JSON.stringify(props.lineage) !== JSON.stringify(prevLineage.current) &&
        props.selectedNode
      ) {
        initGraph()
        buildGraphAll(props.lineage.graph)
      }
      if (props.selectedNode !== prevSelectedNode.current) {
        props.fetchLineage(
          nodeType?.toUpperCase() as JobOrDataset,
          namespace || '',
          nodeName || ''
        )
        getEdges()
      }

      prevLineage.current = props.lineage
      prevSelectedNode.current = props.selectedNode
    }
  })

  React.useEffect(() => {
    // on unmount
    return () => {
      console.log('unmount TODO remove')
      props.resetLineage()
    }
  }, [])

  const initGraph = () => {
    g = new graphlib.Graph<MqNode>({ directed: true })
    g.setGraph(DAGRE_CONFIG)
    g.setDefaultEdgeLabel(() => {
      return {}
    })
  }

  const getEdges = () => {
    const selectedPaths = getSelectedPaths()

    return g?.edges().map(e => {
      const isSelected = selectedPaths.some((r: any) => e.v === r[0] && e.w === r[1])
      return Object.assign(g.edge(e), { isSelected: isSelected })
    })
  }

  const getSelectedPaths = () => {
    const paths = [] as Array<[string, string]>

    const getSuccessors = (node: string) => {
      const successors = g?.successors(node)
      if (successors?.length) {
        for (let i = 0; i < node.length - 1; i++) {
          if (successors[i]) {
            paths.push([node, (successors[i] as unknown) as string])
            getSuccessors((successors[i] as unknown) as string)
          }
        }
      }
    }

    const getPredecessors = (node: string) => {
      const predecessors = g?.predecessors(node)
      if (predecessors?.length) {
        for (let i = 0; i < node.length - 1; i++) {
          if (predecessors[i]) {
            paths.push([(predecessors[i] as unknown) as string, node])
            getPredecessors((predecessors[i] as unknown) as string)
          }
        }
      }
    }

    getSuccessors(props.selectedNode)
    getPredecessors(props.selectedNode)

    return paths
  }

  const buildGraphAll = (graph: LineageNode[]) => {
    // nodes
    for (let i = 0; i < graph.length; i++) {
      g.setNode(graph[i].id, {
        label: graph[i].id,
        data: graph[i].data,
        width: NODE_SIZE,
        height: NODE_SIZE
      })
    }

    // edges
    for (let i = 0; i < graph.length; i++) {
      for (let j = 0; j < graph[i].inEdges.length; j++) {
        g.setEdge(graph[i].inEdges[j].origin, graph[i].id)
      }
    }
    layout(g)

    setState({
      graph: g,
      edges: getEdges(),
      nodes: g.nodes().map(v => g.node(v))
    })
  }

  const i18next = require('i18next')

    return (
      <Box sx={{
        marginTop: `${HEADER_HEIGHT}px`,
        height: `calc(100vh - ${HEADER_HEIGHT}px - ${BOTTOM_OFFSET}px)`
      }}>
        {props.selectedNode === null && (
          <Box display={'flex'} justifyContent={'center'} alignItems={'center'} pt={2}>
            <MqEmpty title={i18next.t('lineage.empty_title')}>
              <MqText subdued>{i18next.t('lineage.empty_body')}</MqText>
            </MqEmpty>
          </Box>
        )}
        {state?.graph && (
          <ParentSize>
            {parent => (
              <Zoom
                width={parent.width}
                height={parent.height}
                scaleXMin={MIN_ZOOM}
                scaleXMax={MAX_ZOOM}
                scaleYMin={MIN_ZOOM}
                scaleYMax={MAX_ZOOM}
                initialTransformMatrix={INITIAL_TRANSFORM}
              >
                {zoom => {
                  return (
                    <Box position='relative'>
                      <svg
                        id={'GRAPH'}
                        width={parent.width}
                        height={parent.height}
                        style={{
                          cursor: zoom.isDragging ? 'grabbing' : 'grab'
                        }}
                      >
                        {/* background */}
                        <g transform={zoom.toString()}>
                          <Edge edgePoints={state?.edges} />
                        </g>
                        <rect
                          width={parent.width}
                          height={parent.height}
                          fill={'transparent'}
                          onTouchStart={zoom.dragStart}
                          onTouchMove={zoom.dragMove}
                          onTouchEnd={zoom.dragEnd}
                          onMouseDown={event => {
                            zoom.dragStart(event)
                          }}
                          onMouseMove={zoom.dragMove}
                          onMouseUp={zoom.dragEnd}
                          onMouseLeave={() => {
                            if (zoom.isDragging) zoom.dragEnd()
                          }}
                          onDoubleClick={event => {
                            const point = localPoint(event) || {
                              x: 0,
                              y: 0
                            }
                            zoom.scale({
                              scaleX: DOUBLE_CLICK_MAGNIFICATION,
                              scaleY: DOUBLE_CLICK_MAGNIFICATION,
                              point
                            })
                          }}
                        />
                        {/* foreground */}
                        <g transform={zoom.toString()}>
                          {state?.nodes.map(node => (
                            <Node
                              key={node.data.name}
                              node={node}
                              selectedNode={props.selectedNode}
                            />
                          ))}
                        </g>
                      </svg>
                    </Box>
                  )
                }}
              </Zoom>
            )}
          </ParentSize>
        )}
      </Box>
    )
}

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
  selectedNode: state.lineage.selectedNode
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode,
      fetchLineage: fetchLineage,
      resetLineage: resetLineage
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Lineage)
