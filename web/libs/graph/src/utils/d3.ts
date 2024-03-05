import { ZoomTransform, zoom, zoomIdentity } from 'd3-zoom'

// An [x, y] pair
type Point = [number, number]

// A [width, height] pair
type Size = [number, number]

/**
 * D3 Zoom uses this format for it's constrain function.
 * Format is [topLeftPoint, bottomRightPoint], which is
 * also [[left, top], [right, bottom]]
 */
export type Extent = [Point, Point]

const extentToSize = (extent: Extent): Size => [
  extent[1][0] - extent[0][0],
  extent[1][1] - extent[0][1],
]

/**
 * Convert an Extent to a DOMRect, which has easier access to x, y, width, height
 * @param extent - scale
 * @returns Rect
 */
export const extentToRect = (
  extent: Extent
): { x: number; y: number; width: number; height: number } => ({
  x: extent[0][0],
  y: extent[0][1],
  width: extent[1][0] - extent[0][0],
  height: extent[1][1] - extent[0][1],
})

/**
 * Add padding around an extent
 * @param extent
 * @param padding - padding value to use for the x axis, and y axis if a third param is not passed.
 * @param [paddingY] - if provided, the padding value to use for the y axis
 * @returns Rect
 */
export const padExtent = (extent: Extent, paddingX: number, paddingY?: number): Extent => [
  [extent[0][0] - paddingX, extent[0][1] - (paddingY === undefined ? paddingX : paddingY)],
  [extent[1][0] + paddingX, extent[1][1] + (paddingY === undefined ? paddingX : paddingY)],
]

/**
 * Returns a new Extent which is large enough to contain two provided Extents
 */
export const maxExtent = (extentA: Extent, extendB: Extent): Extent => [
  [Math.min(extentA[0][0], extendB[0][0]), Math.min(extentA[0][1], extendB[0][1])],
  [Math.max(extentA[1][0], extendB[1][0]), Math.max(extentA[1][1], extendB[1][1])],
]

/**
 * Shortcut for creating a D3 ZoomTransform
 * @param k - scale
 * @param x - horizontal position
 * @param y - vertical position
 * @returns ZoomTransform
 */
export const createZoomTransform = (k: number, x: number, y: number): ZoomTransform =>
  zoomIdentity.translate(x, y).scale(k)

/**
 * Returns the largest scaling factor that will allow the item to fill the container
 * without clipping horizontally or vertically.
 * @param itemExtent
 * @param containerExtent
 * @returns number - the scaling factor
 */
export const scaleToContainer = (itemExtent: Extent, containerExtent: Extent): number => {
  const itemSize = extentToSize(itemExtent)
  const containerSize = extentToSize(containerExtent)

  return Math.min(containerSize[0] / itemSize[0], containerSize[1] / itemSize[1])
}

/**
 * Returns a new D3 ZoomTransform to center the item in a container.
 * This primarily is to better document D3 Zoom's constrain syntax.
 * @param scale - number
 * @param itemExtent
 * @param containerExtent
 * @returns ZoomTransform
 */
export const centerItemInContainer = (
  scale: number,
  itemExtent: Extent,
  containerExtent: Extent
): ZoomTransform =>
  zoom().constrain()(createZoomTransform(scale, 0, 0), containerExtent, itemExtent)

/**
 * Updates the zoom transform to fit the provided extent within the container extent without overscroll
 */
export const constrainZoomToExtent = (
  zoomTransform: ZoomTransform,
  containerExtent: Extent,
  contentExtent: Extent
): ZoomTransform => {
  // Adjust the k value to prevent the need to scroll both left and right, or up and down at the same time.
  const minK = scaleToContainer(contentExtent, containerExtent)
  const zoomAdjustedK =
    zoomTransform.k < minK
      ? createZoomTransform(minK, zoomTransform.x, zoomTransform.y)
      : zoomTransform

  // Calculate the visible extent
  const containerProjection: Extent = [
    zoomAdjustedK.invert([containerExtent[0][0], containerExtent[0][1]]),
    zoomAdjustedK.invert([containerExtent[1][0], containerExtent[1][1]]),
  ]

  const leftOverflow = Math.min(containerProjection[0][0] - contentExtent[0][0], 0) // ignore positive values
  const rightOverflow = Math.max(containerProjection[1][0] - contentExtent[1][0], 0) // ignore negative values

  const topOverflow = Math.min(containerProjection[0][1] - contentExtent[0][1], 0) // ignore positive values
  const bottomOverflow = Math.max(containerProjection[1][1] - contentExtent[1][1], 0) // ignore negative values

  // When the proportions are different, overflow may be required on two sides. In this case, center.
  const xAdjustment =
    leftOverflow && rightOverflow
      ? leftOverflow + rightOverflow // center
      : leftOverflow || rightOverflow

  const yAdjustment =
    topOverflow && bottomOverflow
      ? topOverflow + bottomOverflow // center
      : topOverflow || bottomOverflow

  // Animated the zoom to the new extent
  const zoomAdjusted = zoomAdjustedK.translate(xAdjustment, yAdjustment)

  return zoomAdjusted
}
