import React, { useRef } from 'react'
import useSize from '@react-hook/size'

import { DEFAULT_MAX_SCALE, ZoomPanControls, ZoomPanSvg } from './ZoomPanSvg'
import { Edge as EdgeComponent } from './Edge'
import { Node as NodeComponent } from './Node'
import { useLayout } from '../layout/useLayout'
import Box from '@mui/system/Box'
import LinearProgress from '@mui/material/LinearProgress'
import type { Direction, Edge, Node, NodeRenderer } from '../types'
import type { MiniMapPlacement } from './ZoomPanSvg/MiniMap'

interface Props<K, D> {
  id: string
  nodes: Node<K, D>[]
  edges: Edge[]
  direction?: Direction
  webWorkerUrl?: string
  miniMapPlacement?: MiniMapPlacement
  nodeRenderers: Map<K, NodeRenderer<K, D>>
  width?: number // intended for testing purposes only
  height?: number // intended for testing purposes only
  maxScale?: number
  /*
   * minScale is automatically set to fit the content, but minScaleMinimum can allow the user to scale out further.
   * Typically, this would be set to 1.
   */
  minScaleMinimum?: number
  containerPadding?: number
  emptyMessage?: string
  hideDotGrid?: boolean
  backgroundColor?: string
  dotGridColor?: string
  disableZoomPan?: boolean
  setZoomPanControls?: (controls: ZoomPanControls) => void
}

export const Graph = <K, D>({
  id,
  nodes,
  edges,
  direction,
  webWorkerUrl,
  miniMapPlacement,
  nodeRenderers,
  width: propWidth,
  height: propHeight,
  maxScale = DEFAULT_MAX_SCALE,
  minScaleMinimum,
  containerPadding,
  hideDotGrid = false,
  backgroundColor,
  dotGridColor,
  disableZoomPan = false,
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  setZoomPanControls = () => {},
}: Props<K, D>) => {
  const containerRef = useRef<HTMLDivElement>(null)
  const [containerWidth, containerHeight] = useSize(containerRef)

  const { layout, error, isRendering } = useLayout<K, D>({
    id,
    nodes,
    edges,
    direction: direction ?? 'right',
    keepPreviousGraph: true,
    webWorkerUrl,
    getLayoutOptions: (node: Node<K, D>) =>
      nodeRenderers.get(node.kind)?.getLayoutOptions(node) || node,
  })
  const {
    nodes: positionedNodes,
    edges: positionedEdges,
    width: contentWidth,
    height: contentHeight,
  } = layout || {}

  if (error) {
    console.error(error)
  }

  const measurementsReady =
    (propWidth || containerWidth) &&
    (propHeight || containerHeight) &&
    contentWidth &&
    contentHeight

  return (
    <Box width='100%' height='100%' ref={containerRef} position='relative'>
      {!!positionedNodes?.length && measurementsReady && (
        <ZoomPanSvg
          width={propWidth || containerWidth}
          height={propHeight || containerHeight}
          containerWidth={propWidth || containerWidth}
          containerHeight={propHeight || containerHeight}
          contentWidth={contentWidth}
          contentHeight={contentHeight}
          setZoomPanControls={setZoomPanControls}
          maxScale={maxScale}
          positionedNodes={positionedNodes}
          minScaleMinimum={minScaleMinimum}
          containerPadding={containerPadding}
          miniMapPlacement={miniMapPlacement}
          miniMapContent={
            <>
              {positionedNodes?.map((node) => (
                <NodeComponent<K, D>
                  key={node.id}
                  node={node}
                  nodeRenderers={nodeRenderers}
                  edges={positionedEdges}
                  isMiniMap
                />
              ))}
              {positionedEdges
                ?.filter((edge) => edge.container === id)
                .map((edge) => (
                  <EdgeComponent key={edge.id} edge={edge} isMiniMap />
                ))}
            </>
          }
          hideDotGrid={hideDotGrid}
          backgroundColor={backgroundColor}
          dotGridColor={dotGridColor}
          disabled={disableZoomPan}
        >
          {positionedNodes?.map((node) => (
            <NodeComponent<K, D>
              key={node.id}
              node={node}
              nodeRenderers={nodeRenderers}
              edges={positionedEdges}
            />
          ))}
          {positionedEdges
            ?.filter((edge) => edge.container === id)
            .map((edge) => (
              <EdgeComponent key={edge.id} edge={edge} />
            ))}
        </ZoomPanSvg>
      )}
      {isRendering && (
        <Box position='absolute' bottom='0' left='0' right='0'>
          <LinearProgress />
        </Box>
      )}
    </Box>
  )
}
