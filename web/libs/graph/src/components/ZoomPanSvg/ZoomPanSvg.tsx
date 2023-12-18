import React, { useEffect, useMemo, useRef, useState } from 'react'

import { Box, BoxProps } from '@chakra-ui/react'
import { ZoomTransform, zoom as d3Zoom, zoomIdentity } from 'd3-zoom'
import { useTheme } from '@emotion/react'

import { Background } from './Background'
import {
  Extent,
  centerItemInContainer,
  constrainZoomToExtent,
  createZoomTransform,
  maxExtent,
  padExtent,
  scaleToContainer,
} from '../../utils/d3'
import { MiniMap } from './MiniMap'
import { PositionedNode } from '../../types'
import { createTheme } from '@mui/material'
import { useCallbackRef } from '../../utils/hooks'
import { useD3Selection } from '../../utils/useD3Selection'
import type { MiniMapPlacement } from './MiniMap'

export const clamp = (num: number, min: number, max: number) => Math.min(Math.max(num, min), max)

export const DEFAULT_MAX_SCALE = 1.7
const DEFAULT_ANIMATION_DURATION = 750
const MINIMAP_SCALE = 1 / 8

export interface ZoomPanControls {
  fitContent(): void
  fitExtent(extent: Extent, zoomIn?: boolean): void
  centerOnExtent(extent: Extent): void
  scaleZoom(kDelta?: number): void
  resetZoom(): void
  centerOnPositionedNode(nodeId: string): void
}

interface Props extends BoxProps {
  containerWidth: number
  containerHeight: number
  contentWidth: number
  contentHeight: number
  /*
   * When scaling to an extent, leave this amount of space around it.
   */
  containerPadding?: number
  /*
   * By default the minimap will render the same content as `children`.
   * To render a simplified version, pass `miniMapContent` with the same scale as `children`.
   */
  miniMapContent?: React.ReactNode
  /*
   * Placement of MiniMap within the main Graph container
   */
  miniMapPlacement?: MiniMapPlacement
  /*
   * Limit for zooming in. Defaults to 1.7.
   */
  maxScale?: number
  /*
   * minScale is automatically set to fit the content, but minScaleMinimum can allow the user to scale out further.
   * Typically, this would be set to 1.
   * If `initialExtent` is not set, this can affect the default zoom as well.
   */
  minScaleMinimum?: number
  /*
   * The transition duration for pan and zoom. Defaults to 750.
   * This cannot be called `transitionDuration` because it caused strange tsc errors.
   */
  animationDuration?: number
  /*
   * After showing the entire graph, zoom to this extent on mount.
   */
  initialExtent?: Extent
  /*
   * A callback to access the controls API.
   */
  setZoomPanControls: (controls: ZoomPanControls) => void
  /*
   * Disable dots in the ZoomPanSvg background. Defaults to false.
   */
  hideDotGrid?: boolean
  /*
   * Override the default background color. Defaults to gray.200 in light mode and gray.800 in dark mode.
   */
  backgroundColor?: string
  /*
   * Override the default dot color. Defaults to gray.400 in light mode and gray.400 in dark mode.
   */
  dotGridColor?: string
  /*
   * Disable the ability to zoom and pan. Defaults to false.
   */
  disabled?: boolean
  positionedNodes?: PositionedNode<any, any>[]
}

export const getNodeExtent = (currentNode: PositionedNode<any, any>): Extent => [
  [currentNode.bottomLeftCorner.x, currentNode.bottomLeftCorner.y],
  [
    currentNode.width + currentNode.bottomLeftCorner.x,
    currentNode.height + currentNode.bottomLeftCorner.y,
  ],
]

export const ZoomPanSvg = ({
  containerWidth,
  containerHeight,
  contentWidth,
  contentHeight,
  containerPadding = 0,
  maxScale = DEFAULT_MAX_SCALE,
  minScaleMinimum,
  animationDuration = DEFAULT_ANIMATION_DURATION,
  initialExtent,
  setZoomPanControls,
  miniMapContent,
  miniMapPlacement,
  children,
  hideDotGrid = false,
  backgroundColor,
  dotGridColor,
  disabled = false,
  positionedNodes = [],
  ...otherProps
}: Props) => {
  const theme = createTheme(useTheme())
  const fillColor = theme.palette.secondary.dark

  /* ---- MEASUREMENTS AND LIMITS ---- */

  // These extents are used for determining when items are in view.
  const containerExtent: Extent = useMemo(
    () => [
      [0, 0],
      [containerWidth, containerHeight],
    ],
    [containerWidth, containerHeight]
  )
  const contentExtent: Extent = useMemo(
    () => [
      [0, 0],
      [contentWidth, contentHeight],
    ],
    [contentWidth, contentHeight]
  )

  // Maximum zoom out, which is minScale, should only be enough to show the entire graph
  const fitContentScale = Math.min(scaleToContainer(contentExtent, containerExtent), maxScale)
  const minScale = minScaleMinimum ? Math.min(fitContentScale, minScaleMinimum) : fitContentScale

  // default scale shows the entire graph
  const defaultZoom = useMemo(
    () =>
      centerItemInContainer(minScale, padExtent(contentExtent, containerPadding), containerExtent),
    [containerExtent, contentExtent, containerPadding, minScale]
  )

  // The scroll/pan limits. These are set to 1/8 of the display size at minScale so that the user can pan content past the minimap and compare
  const translateExtent: Extent = useMemo(() => {
    // Add padding around the content to provide room the minimap and compare
    const scaledXPadding = (containerWidth * MINIMAP_SCALE + containerPadding) / minScale
    const scaledYPadding = (containerHeight * MINIMAP_SCALE + containerPadding) / minScale

    // Add padding around the content to account for the minimap and compare
    const contentScrollExtent: Extent = padExtent(contentExtent, scaledXPadding, scaledYPadding)

    // A projection of the container extent when showing the entire graph
    const containerProjectionExtent: Extent = [
      defaultZoom.invert([0, 0]),
      defaultZoom.invert([containerWidth, containerHeight]),
    ]

    return maxExtent(contentScrollExtent, containerProjectionExtent)
  }, [defaultZoom, contentExtent, containerWidth, containerHeight, containerPadding, minScale])

  /* ---- STATE ---- */

  const [currentZoomState, setCurrentZoomState] = useState<typeof defaultZoom>(defaultZoom)
  const [grabbing, setGrabbing] = useState<boolean>(false)

  /* ---- REFS ---- */

  const svgRef = useRef<SVGSVGElement>(null)
  const zoomRef = useRef<SVGGElement>(null)
  const svgD3Selection = useD3Selection(svgRef)
  const zoomD3Selection = useD3Selection(zoomRef)

  /* ---- UPDATE ZOOM STATE ----
   * These functions need to be elevated and/or moved to context to enable the zoom control bar and compare.
   */

  const animateToZoomState = (transform: ZoomTransform) => {
    // skip this operation in jest and when the selections are not ready
    if (!svgRef.current?.transform?.baseVal || !zoomD3Selection || !svgD3Selection) return

    // constrain to the zoom and pan limits
    const transformConstrained = constrainZoomToExtent(
      createZoomTransform(clamp(transform.k, minScale, maxScale), transform.x, transform.y),
      containerExtent,
      translateExtent
    )

    // If the all values are the same, skip the updates
    if (
      transformConstrained.x === currentZoomState.x &&
      transformConstrained.y === currentZoomState.y &&
      transformConstrained.k === currentZoomState.k
    ) {
      return
    }

    // Update the minimap without animation
    setCurrentZoomState(transformConstrained)

    // Animate the zoom update
    zoomD3Selection
      ?.transition()
      .duration(animationDuration)
      .attr('transform', transformConstrained.toString())

    // Update the zoomBehavior so future events have the correct transform diff
    svgD3Selection
      ?.transition()
      .duration(animationDuration)
      .call(d3Zoom<SVGSVGElement, unknown>().transform, transformConstrained)
  }

  // Reset to x:0, y:0, k:1
  const resetZoom = () => {
    animateToZoomState(zoomIdentity)
  }

  const centerOnPositionedNode = (nodeId: string) => {
    const node = positionedNodes.find((node) => node.id === nodeId)
    if (!node) return

    const extent = getNodeExtent(node)
    centerOnExtent(extent)
  }

  const fitContent = () => {
    animateToZoomState(defaultZoom)
  }

  const fitExtent = (extent: Extent, zoomIn = true) => {
    const max = zoomIn ? maxScale : currentZoomState.k
    const k = clamp(
      scaleToContainer(padExtent(extent, containerPadding), containerExtent),
      minScale,
      max
    )
    const transform = centerItemInContainer(k, extent, containerExtent)

    animateToZoomState(transform)
  }

  const centerOnExtent = (extent: Extent, k = currentZoomState.k) => {
    const transform = centerItemInContainer(k, extent, containerExtent)

    animateToZoomState(transform)
  }

  const scaleZoom = (scaleFactor: number) => {
    const { k: oldK } = currentZoomState
    const newK = clamp(oldK * scaleFactor, minScale, maxScale)

    if (newK === oldK) return

    // Convert the center of the container to a point in the graph
    const center = currentZoomState.invert([containerWidth / 2, containerHeight / 2])

    // zero width/height Extent at the center of the graph
    const centerExtent: Extent = [center, center]

    // Set the newK, while maintain the current center
    const transform = centerItemInContainer(newK, centerExtent, containerExtent)

    animateToZoomState(transform)
  }

  /* ---- EVENT HANDLERS ---- */
  const handleMount = () => {
    // Zoom to show the entire graph
    zoomD3Selection?.attr('transform', defaultZoom.toString())
    // Zoom to show the initialExtent
    if (initialExtent) {
      centerOnExtent(initialExtent, 1.4)
    } else {
      // skip this operation in jest and when the selections are not ready
      if (!svgRef.current?.transform?.baseVal || !svgD3Selection) return

      // Update the minimap without animation
      setCurrentZoomState(defaultZoom)
      svgD3Selection?.call(d3Zoom<SVGSVGElement, unknown>().transform, defaultZoom)
    }
  }

  const handleResize = () => {
    // animateToZoomState constrains the zoom to translateExtent (pan limits)
    animateToZoomState(currentZoomState)
  }

  /* ---- CALLBACK REFS ---- */
  // These create static references for hooks and children, unlike useCallback.
  const handleMountRef = useCallbackRef(handleMount)
  const handleResizeRef = useCallbackRef(handleResize)
  // Zoom Controls
  const fitContentRef = useCallbackRef(fitContent)
  const fitExtentRef = useCallbackRef(fitExtent)
  const centerOnExtentRef = useCallbackRef(centerOnExtent)
  const scaleZoomRef = useCallbackRef(scaleZoom)
  const resetZoomRef = useCallbackRef(resetZoom)
  const centerOnPositionedNodeRef = useCallbackRef(centerOnPositionedNode)

  /* ---- EFFECTS ---- */

  // Initialize or update the zoom behavior
  useEffect(() => {
    if (svgD3Selection && zoomD3Selection && !disabled) {
      svgD3Selection.on('mousedown', () => {
        setGrabbing(true)
      })

      const zoomBehavior = d3Zoom<SVGSVGElement, unknown>()
        .scaleExtent([minScale, maxScale])
        .translateExtent(translateExtent)
        .on('zoom', (event) => {
          const transform = event.transform as ZoomTransform
          setCurrentZoomState(transform)

          zoomD3Selection.attr('transform', transform.toString())
        })
        .on('end', () => {
          setGrabbing(false)
        })

      svgD3Selection.call(zoomBehavior)
    }
  }, [svgD3Selection, zoomD3Selection, minScale, maxScale, translateExtent, disabled])

  // Update the zoom api to control refs
  useEffect(
    () =>
      setZoomPanControls({
        fitContent: fitContentRef,
        fitExtent: fitExtentRef,
        centerOnExtent: centerOnExtentRef,
        scaleZoom: scaleZoomRef,
        resetZoom: resetZoomRef,
        centerOnPositionedNode: centerOnPositionedNodeRef,
      }),
    [setZoomPanControls, fitContentRef, fitExtentRef, centerOnExtentRef, scaleZoomRef, resetZoomRef]
  )

  // On first load
  useEffect(handleMountRef, [handleMountRef, svgD3Selection, zoomD3Selection])

  // Resize of container or content
  useEffect(handleResizeRef, [handleResizeRef, translateExtent])

  // Choose the correct cursor based on if the ZoomPanSvg is disabled
  // or it is in grabbing state
  const cursor = useMemo(() => {
    if (disabled) return 'auto'
    if (grabbing) return 'grabbing'
    return 'grab'
  }, [grabbing, disabled])

  return (
    <Box
      position='relative'
      overflow='hidden'
      height='100%'
      width='100%'
      bgColor={fillColor}
      {...otherProps}
    >
      <MiniMap
        containerWidth={containerWidth}
        containerHeight={containerHeight}
        contentWidth={contentWidth}
        contentHeight={contentHeight}
        zoomTransform={currentZoomState}
        miniMapScale={MINIMAP_SCALE}
        placement={miniMapPlacement}
        backgroundColor={theme.palette.background.paper}
      >
        {miniMapContent}
      </MiniMap>
      <svg
        ref={svgRef}
        data-testid='zoom-pan-svg'
        style={{
          width: '100%',
          height: '100%',
          cursor,
          userSelect: 'none',
        }}
      >
        <g
          data-testid='zoom-pan-group'
          ref={zoomRef}
          style={{
            height: containerHeight,
            width: containerWidth,
          }}
        >
          <>
            <Background
              k={currentZoomState.k}
              contentWidth={contentWidth}
              contentHeight={contentHeight}
              hideDotGrid={hideDotGrid}
              backgroundColor={backgroundColor}
              dotGridColor={dotGridColor}
            />
            {children}
          </>
        </g>
      </svg>
    </Box>
  )
}
