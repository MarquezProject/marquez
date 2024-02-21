import React, { ReactNode } from 'react'

import { Box, BoxProps } from '@chakra-ui/react'
import type { ZoomTransform } from 'd3-zoom'

import { grey } from '@mui/material/colors'

import { Extent, centerItemInContainer, extentToRect, scaleToContainer } from '../../utils/d3'

interface Props extends BoxProps {
  containerWidth: number
  containerHeight: number
  contentWidth: number
  contentHeight: number
  miniMapScale: number // The ratio of the MiniMap container to the main Graph container.
  zoomTransform: ZoomTransform
  placement?: MiniMapPlacement
  children: ReactNode
}

export enum MiniMapPlacement {
  TopLeft = 'top-left',
  TopRight = 'top-right',
  BottomRight = 'bottom-right',
  BottomLeft = 'bottom-left',
  None = 'none',
}

export const MiniMap = ({
  containerWidth,
  containerHeight,
  contentWidth,
  contentHeight,
  zoomTransform,
  miniMapScale,
  placement = MiniMapPlacement.BottomLeft,
  children,
  ...otherProps
}: Props) => {
  const backgroundColor = grey['700']
  const borderColor = 'black'
  const maskColor = 'black'
  const maskOpacity = '.5'
  const width = Math.round(containerWidth * miniMapScale)
  const height = Math.round(containerHeight * miniMapScale)

  const minimapExtent: Extent = [
    [0, 0],
    [width, height],
  ]
  const graphExtent: Extent = [
    [0, 0],
    [contentWidth, contentHeight],
  ]

  // The ratio of the Minimap to the Layout data size (Fit to the container)
  const layoutScale = scaleToContainer(graphExtent, minimapExtent)

  // Determine the zoom level of the mini map relative to the transformation of the main map.
  const centerGraphTransform = centerItemInContainer(layoutScale, graphExtent, minimapExtent)

  // Invert the viewPort to determine the lens position.
  const lensExtent: Extent = [
    zoomTransform.invert([0, 0]),
    zoomTransform.invert([containerWidth, containerHeight]),
  ]
  const lensRect = extentToRect(lensExtent)

  return placement !== MiniMapPlacement.None ? (
    <Box
      position='absolute'
      top={
        placement === MiniMapPlacement.TopLeft || placement === MiniMapPlacement.TopRight
          ? 4
          : undefined
      }
      bottom={
        placement === MiniMapPlacement.BottomLeft || placement === MiniMapPlacement.BottomRight
          ? 4
          : undefined
      }
      right={
        placement === MiniMapPlacement.TopRight || placement === MiniMapPlacement.BottomRight
          ? 4
          : undefined
      }
      left={
        placement === MiniMapPlacement.TopLeft || placement === MiniMapPlacement.BottomLeft
          ? 4
          : undefined
      }
      width={`${width}px`}
      height={`${height}px`}
      backgroundColor={backgroundColor}
      borderColor={borderColor}
      borderWidth='1px'
      borderRadius='md'
      zIndex='2'
      {...otherProps}
    >
      <svg width='100%' height='100%'>
        <g transform={centerGraphTransform.toString()}>{children}</g>
        <mask id='miniMapMask'>
          <rect x={0} y={0} rx={4} width='100%' height='100%' fill='white' />
          <rect
            transform={centerGraphTransform.toString()}
            x={lensRect.x}
            y={lensRect.y}
            rx={4}
            width={lensRect.width}
            height={lensRect.height}
          />
        </mask>
        <rect
          x={0}
          y={0}
          rx={4}
          width='100%'
          height='100%'
          opacity={maskOpacity}
          fill={maskColor}
          mask='url(#miniMapMask)'
        />
      </svg>
    </Box>
  ) : null
}
