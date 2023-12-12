import * as Redux from 'redux'
import { ColumnLineageGraph } from '../../types/api'
import { Edge, Node as ElkNode, Graph } from '../../../libs/graph'
import { IState } from '../../store/reducers'
import { MultipleNodeData, MultipleNodeKind, columnLevelNodeRenderer } from './nodes'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchColumnLineage } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React, { useEffect } from 'react'

interface StateProps {
  columnLineage: ColumnLineageGraph
}

interface DispatchProps {
  fetchColumnLineage: typeof fetchColumnLineage
}

type ColumnLevelProps = StateProps & DispatchProps

export const TRANSITION_DURATION = 750

/*
   Node of format dataset:food_delivery:public.categories:menu_id
   Node of format {type}:{namespace}:{table}:{column}
 */
export const parseColumnLineageNode = (node: string) => {
  const [type, namespace, dataset, column] = node.split(':')
  return { type, namespace, dataset, column }
}

export const createElkNodes = (columnLineageGraph: ColumnLineageGraph) => {
  const nodes: ElkNode<MultipleNodeKind, MultipleNodeData>[] = []
  const edges: Edge[] = []
  for (const node of columnLineageGraph.graph) {
    const { type, namespace, dataset, column } = parseColumnLineageNode(node.id)

    edges.push(
      ...node.outEdges.map((edge) => {
        return {
          id: `${edge.origin}:${edge.destination}`,
          sourceNodeId: edge.origin,
          targetNodeId: edge.destination,
        }
      })
    )

    const datasetNode = nodes.find((n) => n.id === `${type}:${namespace}:${dataset}`)
    if (!datasetNode) {
      nodes.push({
        id: `${type}:${namespace}:${dataset}`,
        kind: 'dataset',
        width: 800,
        data: {
          namespace,
          dataset,
        },
        children: [
          {
            id: node.id,
            height: 24,
            width: 200,
            kind: 'column',
            data: {
              column,
            },
          },
        ],
      })
    } else {
      datasetNode.children?.push({
        id: node.id,
        width: 200,
        height: 24,
        kind: 'column',
        data: {
          column,
        },
      })
    }
  }
  return { nodes, edges }
}

const ColumnLevel: React.FC<ColumnLevelProps> = ({
  fetchColumnLineage: fetchColumnLineage,
  columnLineage: columnLineage,
}: ColumnLevelProps) => {
  const { namespace, name } = useParams()
  useEffect(() => {
    if (name && namespace) {
      fetchColumnLineage('DATASET', namespace, name, 10)
    }
  }, [name, namespace])

  if (!columnLineage) {
    return <div />
  }

  const { nodes, edges } = createElkNodes(columnLineage)

  return (
    <Box height={'calc(100vh - 98px)'}>
      <ParentSize>
        {(parent) => {
          console.log(parent)
          return (
            <Graph<MultipleNodeKind, MultipleNodeData>
              id='column-level-graph'
              backgroundColor={theme.palette.background.default}
              height={parent.height}
              width={parent.width}
              nodes={nodes}
              edges={edges}
              direction='right'
              nodeRenderers={columnLevelNodeRenderer}
            />
          )
        }}
      </ParentSize>
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  columnLineage: state.columnLineage.columnLineage,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchColumnLineage: fetchColumnLineage,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(ColumnLevel)
