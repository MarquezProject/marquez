// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { LegacyRef } from 'react'

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
import {
  fetchLineage,
  resetLineage,
  setLineageGraphDepth,
  setSelectedNode,
} from '../../store/actionCreators'
import { generateNodeId } from '../../helpers/nodes'
import { localPoint } from '@visx/event'
import { useParams } from 'react-router-dom'
import DepthConfig from './components/depth-config/DepthConfig'
import Edge from './components/edge/Edge'
import FullGraphSwitch from '../full-graph-switch/FullGraphSwitch'
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
  depth: number
  showFullGraph: boolean
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

export function initGraph() {
  const g = new graphlib.Graph<MqNode>({ directed: true })
  g.setGraph(DAGRE_CONFIG)
  g.setDefaultEdgeLabel(() => {
    return {}
  })

  return g
}

export function buildGraphAll(
  g: graphlib.Graph<MqNode>,
  graph: LineageNode[],
  fullGraph: boolean,
  selectedNode: string,
  callBack: (g: graphlib.Graph<MqNode>) => void
) {
  // nodes
  for (let i = 0; i < graph.length; i++) {
    g.setNode(graph[i].id, {
      label: graph[i].id,
      data: graph[i].data,
      width: NODE_SIZE,
      height: NODE_SIZE,
    })
  }

  // edges
  for (let i = 0; i < graph.length; i++) {
    for (let j = 0; j < graph[i].inEdges.length; j++) {
      g.setEdge(graph[i].inEdges[j].origin, graph[i].id)
    }
  }

  if (!fullGraph) {
    removeUnselectedNodes(g, selectedNode)
  }
  layout(g)

  callBack(g)
}

export function getSelectedPaths(g: graphlib.Graph<MqNode>, selectedNode: string) {
  const paths = [] as Array<[string, string]>

  // Sets used to detect cycles and break out of the recursive loop
  const visitedNodes = {
    successors: new Set(),
    predecessors: new Set(),
  }

  const getSuccessors = (node: string) => {
    if (visitedNodes.successors.has(node)) return
    visitedNodes.successors.add(node)

    const successors = g?.successors(node)
    if (successors?.length) {
      for (let i = 0; i < successors.length; i++) {
        if (successors[i]) {
          paths.push([node, successors[i] as unknown as string])
          getSuccessors(successors[i] as unknown as string)
        }
      }
    }
  }

  const getPredecessors = (node: string) => {
    if (visitedNodes.predecessors.has(node)) return
    visitedNodes.predecessors.add(node)

    const predecessors = g?.predecessors(node)
    if (predecessors?.length) {
      for (let i = 0; i < predecessors.length; i++) {
        if (predecessors[i]) {
          paths.push([predecessors[i] as unknown as string, node])
          getPredecessors(predecessors[i] as unknown as string)
        }
      }
    }
  }

  getSuccessors(selectedNode)
  getPredecessors(selectedNode)

  return paths
}

export function removeUnselectedNodes(g: graphlib.Graph<MqNode>, selectedNode: string) {
  const nodesInSelectedPath = new Set(getSelectedPaths(g, selectedNode).flat())
  const nodesToRemove = g.nodes().filter((n) => !nodesInSelectedPath.has(n))

  for (const node of nodesToRemove) {
    g.removeNode(node)
  }
}

export interface LineageProps extends StateProps, DispatchProps {}

let g: graphlib.Graph<MqNode>

const Lineage: React.FC<LineageProps> = (props: LineageProps) => {
  const [state, setState] = React.useState<LineageState>({
    graph: g,
    edges: [],
    nodes: [],
  })
  const { nodeName, namespace, nodeType } = useParams()
  const mounted = React.useRef<boolean>(false)

  const prevLineage = React.useRef<LineageGraph>()
  const prevDepth = React.useRef<number>()
  const prevSelectedNode = React.useRef<string>()
  const prevShowFullGraph = React.useRef<boolean>()

  React.useEffect(() => {
    if (!mounted.current) {
      // on mount
      if (nodeName && namespace && nodeType) {
        const nodeId = generateNodeId(nodeType.toUpperCase() as JobOrDataset, namespace, nodeName)
        props.setSelectedNode(nodeId)

        props.fetchLineage(nodeType.toUpperCase() as JobOrDataset, namespace, nodeName, props.depth)
      }
      mounted.current = true
    } else {
      // on update
      if (
        (JSON.stringify(props.lineage) !== JSON.stringify(prevLineage.current) ||
          props.depth !== prevDepth.current ||
          props.showFullGraph !== prevShowFullGraph.current) &&
        props.selectedNode
      ) {
        g = initGraph()
        buildGraphAll(
          g,
          props.lineage.graph,
          props.showFullGraph,
          props.selectedNode,
          (gResult: graphlib.Graph<MqNode>) => {
            setState({
              graph: gResult,
              edges: getEdges(),
              nodes: gResult.nodes().map((v) => gResult.node(v)),
            })
          }
        )
      }
      if (props.selectedNode !== prevSelectedNode.current || props.depth !== prevDepth.current) {
        props.fetchLineage(
          nodeType?.toUpperCase() as JobOrDataset,
          namespace || '',
          nodeName || '',
          props.depth
        )
        getEdges()
      }

      if (props.selectedNode !== prevSelectedNode.current && !props.showFullGraph) {
        // Always render the graph if the selected node changes and we aren't showing the full graph
        // since new nodes may need to be added or removed from view
        buildGraphAll(
          g,
          props.lineage.graph,
          props.showFullGraph,
          props.selectedNode,
          (gResult: graphlib.Graph<MqNode>) => {
            setState({
              graph: gResult,
              edges: getEdges(),
              nodes: gResult.nodes().map((v) => gResult.node(v)),
            })
          }
        )
      }
      prevLineage.current = props.lineage
      prevDepth.current = props.depth
      prevSelectedNode.current = props.selectedNode
      prevShowFullGraph.current = props.showFullGraph
    }
  })

  React.useEffect(() => {
    // on unmount
    return () => {
      props.resetLineage()
    }
  }, [])

  const getEdges = () => {
    const selectedPaths = getSelectedPaths(g, props.selectedNode)

    return g?.edges().map((e) => {
      const isSelected = selectedPaths.some((r: any) => e.v === r[0] && e.w === r[1])
      return Object.assign(g.edge(e), { isSelected: isSelected })
    })
  }

  const i18next = require('i18next')

  return (
    <Box
      sx={{
        marginTop: `${HEADER_HEIGHT}px`,
        height: `calc(100vh - ${HEADER_HEIGHT}px - ${BOTTOM_OFFSET}px)`,
      }}
    >
      {props.selectedNode === null && (
        <Box display={'flex'} justifyContent={'center'} alignItems={'center'} pt={2}>
          <MqEmpty title={i18next.t('lineage.empty_title')}>
            <MqText subdued>{i18next.t('lineage.empty_body')}</MqText>
          </MqEmpty>
        </Box>
      )}
      <Box
        sx={(theme) => ({
          zIndex: theme.zIndex.appBar + 1,
          position: 'absolute',
          right: 0,
          margin: '1rem 3rem',
        })}
      >
        <DepthConfig depth={props.depth} />
        <FullGraphSwitch />
      </Box>
      {state?.graph && (
        <ParentSize>
          {(parent) => (
            <Zoom
              width={parent.width}
              height={parent.height}
              scaleXMin={MIN_ZOOM}
              scaleXMax={MAX_ZOOM}
              scaleYMin={MIN_ZOOM}
              scaleYMax={MAX_ZOOM}
              initialTransformMatrix={INITIAL_TRANSFORM}
            >
              {(zoom) => (
                <div>
                  <svg
                    id={'GRAPH'}
                    width={parent.width}
                    height={parent.height}
                    style={{
                      cursor: zoom.isDragging ? 'grabbing' : 'grab',
                    }}
                    ref={zoom.containerRef as LegacyRef<SVGSVGElement>}
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
                      onMouseDown={(event) => {
                        zoom.dragStart(event)
                      }}
                      onMouseMove={zoom.dragMove}
                      onMouseUp={zoom.dragEnd}
                      onMouseLeave={() => {
                        if (zoom.isDragging) zoom.dragEnd()
                      }}
                      onDoubleClick={(event) => {
                        const point = localPoint(event) || {
                          x: 0,
                          y: 0,
                        }
                        zoom.scale({
                          scaleX: DOUBLE_CLICK_MAGNIFICATION,
                          scaleY: DOUBLE_CLICK_MAGNIFICATION,
                          point,
                        })
                      }}
                    />
                    {/* foreground */}
                    <g transform={zoom.toString()}>
                      {state?.nodes.map((node) => (
                        <Node key={node.data.name} node={node} selectedNode={props.selectedNode} />
                      ))}
                    </g>
                  </svg>
                </div>
              )}
            </Zoom>
          )}
        </ParentSize>
      )}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
  selectedNode: state.lineage.selectedNode,
  depth: state.lineage.depth,
  showFullGraph: state.lineage.showFullGraph,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode,
      fetchLineage: fetchLineage,
      resetLineage: resetLineage,
      setDepth: setLineageGraphDepth,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Lineage)
