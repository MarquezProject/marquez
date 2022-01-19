// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import * as Redux from 'redux'
import { Box } from '@material-ui/core'
import { DAGRE_CONFIG, INITIAL_TRANSFORM, NODE_SIZE } from './config'
import { GraphEdge, Node as GraphNode, graphlib, layout } from 'dagre'
import { HEADER_HEIGHT } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { JobOrDataset, LineageNode, MqNode } from './types'
import { LineageGraph } from '../../types/api'
import { RouteComponentProps, withRouter } from 'react-router-dom'
import { WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { Zoom } from '@visx/zoom'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchLineage, resetLineage, setSelectedNode } from '../../store/actionCreators'
import { generateNodeId } from '../../helpers/nodes'
import { localPoint } from '@visx/event'
import Edge from './components/edge/Edge'
import MqEmpty from '../core/empty/MqEmpty'
import MqText from '../core/text/MqText'
import Node from './components/node/Node'
import ParentSize from '@visx/responsive/lib/components/ParentSize'

const BOTTOM_OFFSET = 8

const styles = () => {
  return createStyles({
    lineageContainer: {
      marginTop: HEADER_HEIGHT,
      height: `calc(100vh - ${HEADER_HEIGHT}px - ${BOTTOM_OFFSET}px)`
    }
  })
}

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

type LineageProps = WithStyles<typeof styles> &
  StateProps &
  DispatchProps &
  RouteComponentProps<JobOrDatasetMatchParams>

let g: graphlib.Graph<MqNode>

class Lineage extends React.Component<LineageProps, LineageState> {
  constructor(props: LineageProps) {
    super(props)
    this.state = {
      graph: g,
      edges: [],
      nodes: []
    }
  }

  componentDidMount() {
    const nodeName = this.props.match.params.nodeName
    const namespace = this.props.match.params.namespace
    const nodeType = this.props.match.params.nodeType
    if (nodeName && namespace && nodeType) {
      const nodeId = generateNodeId(
        this.props.match.params.nodeType.toUpperCase() as JobOrDataset,
        this.props.match.params.namespace,
        this.props.match.params.nodeName
      )
      this.props.setSelectedNode(nodeId)
      this.props.fetchLineage(
        this.props.match.params.nodeType.toUpperCase() as JobOrDataset,
        this.props.match.params.namespace,
        this.props.match.params.nodeName
      )
    }
  }

  componentDidUpdate(prevProps: Readonly<LineageProps>) {
    if (
      JSON.stringify(this.props.lineage) !== JSON.stringify(prevProps.lineage) &&
      this.props.selectedNode
    ) {
      this.initGraph()
      this.buildGraphAll(this.props.lineage.graph)
    }
    if (this.props.selectedNode !== prevProps.selectedNode) {
      this.props.fetchLineage(
        this.props.match.params.nodeType.toUpperCase() as JobOrDataset,
        this.props.match.params.namespace,
        this.props.match.params.nodeName
      )
    }
  }

  componentWillUnmount() {
    this.props.resetLineage()
  }

  initGraph = () => {
    g = new graphlib.Graph<MqNode>({ directed: true })
    g.setGraph(DAGRE_CONFIG)
    g.setDefaultEdgeLabel(() => {
      return {}
    })
  }

  buildGraphAll = (graph: LineageNode[]) => {
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

    this.setState({
      graph: g,
      edges: g.edges().map(e => g.edge(e)),
      nodes: g.nodes().map(v => g.node(v))
    })
  }

  render() {
    const { classes } = this.props
    return (
      <Box className={classes.lineageContainer}>
        {this.props.selectedNode === null && (
          <Box display={'flex'} justifyContent={'center'} alignItems={'center'} pt={2}>
            <MqEmpty title={'No node selected'}>
              <MqText subdued>
                Try selecting a node through search or the jobs or datasets page.
              </MqText>
            </MqEmpty>
          </Box>
        )}
        {this.state.graph && (
          <ParentSize>
            {parent => (
              <Zoom
                width={parent.width}
                height={parent.height}
                scaleXMin={MIN_ZOOM}
                scaleXMax={MAX_ZOOM}
                scaleYMin={MIN_ZOOM}
                scaleYMax={MAX_ZOOM}
                transformMatrix={INITIAL_TRANSFORM}
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
                          <Edge edgePoints={this.state.edges} />
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
                          {this.state.nodes.map(node => (
                            <Node
                              key={node.data.name}
                              node={node}
                              edgeEnds={this.state.edges.map(
                                edge => edge.points[edge.points.length - 1]
                              )}
                              selectedNode={this.props.selectedNode}
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

export default withStyles(styles)(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(withRouter(Lineage))
)
