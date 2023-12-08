import * as Redux from 'redux'
import { Box } from '@mui/material'
import { ColumnLevelDatasetData, customNodesRenderers } from './ColumnLevelDatasetNode'
import { ColumnLineageGraph } from '../../types/api'
import { Edge as EdgeComponent } from '../../../libs/graph/src/components/Edge'
import { IState } from '../../store/reducers'
import { Node as NodeComponent } from '../../../libs/graph/src/components/Node'
import { ZoomPanControls, ZoomPanSvg } from '../../../libs/graph'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { edges, nodes } from './nodes'
import { fetchColumnLineage } from '../../store/actionCreators'
import { useCallbackRef } from '@chakra-ui/react'
import { useLayout } from '../../../libs/graph/src/layout/useLayout'
import { useParams } from 'react-router-dom'
import React, { useDeferredValue, useEffect, useRef } from 'react'

interface OwnProps {
  width: number
  height: number
}

interface StateProps {
  columnLineage: ColumnLineageGraph
}

interface DispatchProps {
  fetchColumnLineage: typeof fetchColumnLineage
}

type ColumnLevelProps = OwnProps & StateProps & DispatchProps

export const TRANSITION_DURATION = 750

const ColumnLevel: React.FC<ColumnLevelProps> = ({
  width: widthPropValue,
  height: heightPropValue,
  fetchColumnLineage: fetchColumnLineage,
  columnLineage: columnLineage,
}: ColumnLevelProps) => {
  const { namespace, name } = useParams()
  useEffect(() => {
    if (name && namespace) {
      fetchColumnLineage('DATASET', namespace, name, 10)
    }
  }, [name, namespace])

  const { layout, error, isRendering } = useLayout<'simple', ColumnLevelDatasetData>({
    id: 'graph',
    nodes: nodes,
    edges: edges,
    direction: 'right',
    keepPreviousGraph: false,
    webWorkerUrl: '',
    getLayoutOptions: (node) => customNodesRenderers.get(node.kind)?.getLayoutOptions(node) || node,
  })

  const {
    nodes: positionedNodes,
    edges: positionedEdges,
    width: contentWidth,
    height: contentHeight,
  } = layout || {}

  const measurementsReady = contentWidth && contentHeight

  const zoomPanControlsRef = useRef<ZoomPanControls>()

  const width = useDeferredValue(widthPropValue)
  const height = useDeferredValue(heightPropValue)

  const setZoomPanControls = useCallbackRef((controls) => {
    zoomPanControlsRef.current = controls
    // setGraphControls({
    //   centerOnCurrentNode: centerOnCurrentNodeRef,
    //   centerOnNodeId: centerOnNodeIdRef,
    //   ...controls,
    // })
  })

  return (
    <Box position={'absolute'} width={width} height={height}>
      <ZoomPanSvg
        containerWidth={width}
        containerHeight={height}
        contentWidth={width}
        contentHeight={height}
        containerPadding={0}
        maxScale={4}
        animationDuration={TRANSITION_DURATION}
        setZoomPanControls={setZoomPanControls}
        miniMapContent={
          <>
            {positionedNodes?.map((node) => (
              <NodeComponent<'simple', ColumnLevelDatasetData>
                key={node.id}
                node={node}
                nodeRenderers={customNodesRenderers}
                edges={positionedEdges}
              />
            ))}
            {positionedEdges?.map((edge) => (
              <EdgeComponent key={edge.id} edge={edge} />
            ))}
          </>
        }
      >
        {positionedNodes?.map((node) => (
          <NodeComponent<'simple', ColumnLevelDatasetData>
            key={node.id}
            node={node}
            nodeRenderers={customNodesRenderers}
            edges={positionedEdges}
          />
        ))}
        {positionedEdges?.map((edge) => (
          <EdgeComponent key={edge.id} edge={edge} />
        ))}
      </ZoomPanSvg>
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
