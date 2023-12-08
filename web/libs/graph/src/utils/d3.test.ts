import {
  constrainZoomToExtent,
  createZoomTransform,
  maxExtent,
  padExtent,
  scaleToContainer,
} from './d3'

/*
 * These random values are used to define the origin, so it's
 * easier to only set (and understand) the width and height
 * of the items and containers in each test scenario.
 */
const itemOffset = 76
const containerOffset = 62

describe('d3 tests', () => {
  /**
   * The item may be scaled up 3 times before filling the vertical space
   */
  test('padExtent ', () => {
    expect(
      padExtent(
        [
          [100, 100],
          [200, 200],
        ],
        20
      )
    ).toEqual([
      [80, 80],
      [220, 220],
    ])
  })
  /**
   * The item may be scaled up 3 times before filling the vertical space
   */
  test('scale wide container ', () => {
    expect(
      scaleToContainer(
        [
          [itemOffset, itemOffset],
          [10 + itemOffset, 20 + itemOffset],
        ],
        [
          [containerOffset, containerOffset],
          [100 + containerOffset, 60 + containerOffset],
        ]
      )
    ).toEqual(3)
  })
  /**
   * The item may be scaled up 5 times before filling the horizontal space
   */
  test('scale to tall container ', () => {
    expect(
      scaleToContainer(
        [
          [itemOffset, itemOffset],
          [10 + itemOffset, 20 + itemOffset],
        ],
        [
          [containerOffset, containerOffset],
          [60 + containerOffset, 100 + containerOffset],
        ]
      )
    ).toEqual(5)
  })
  /**
   * The item must be scaled down 4 times to fit the vertical space
   */
  test('scale to smaller wide container ', () => {
    expect(
      scaleToContainer(
        [
          [itemOffset, itemOffset],
          [100 + itemOffset, 60 + itemOffset],
        ],
        [
          [containerOffset, containerOffset],
          [50 + containerOffset, 15 + containerOffset],
        ]
      )
    ).toEqual(0.25)
  })
  /**
   * The item must be scaled down 4 times to fit the horizontal space
   */
  test('scale to smaller tall container ', () => {
    expect(
      scaleToContainer(
        [
          [itemOffset, itemOffset],
          [60 + itemOffset, 100 + itemOffset],
        ],
        [
          [containerOffset, containerOffset],
          [15 + containerOffset, 50 + containerOffset],
        ]
      )
    ).toEqual(0.25)
  })

  test('maxExtent with narrow in tall', () => {
    expect(
      maxExtent(
        [
          [-20, 20],
          [120, 40],
        ],
        [
          [20, -40],
          [60, 120],
        ]
      )
    ).toEqual([
      [-20, -40],
      [120, 120],
    ])
  })

  test('maxExtent with tall in narrow ', () => {
    expect(
      maxExtent(
        [
          [20, -40],
          [60, 120],
        ],
        [
          [-20, 20],
          [120, 40],
        ]
      )
    ).toEqual([
      [-20, -40],
      [120, 120],
    ])
  })

  test('constrainZoomToExtent with invalid zoom and scroll', () => {
    expect(
      constrainZoomToExtent(
        createZoomTransform(0.25, 100, 100),
        [
          [0, 0],
          [100, 100],
        ],
        [
          [0, 0],
          [200, 200],
        ]
      )
    ).toEqual({
      k: 0.5, // min zoom is applied
      x: 0, // scroll left
      y: 0, // scrolled up
    })
  })

  test('constrainZoomToExtent with invalid scroll', () => {
    expect(
      constrainZoomToExtent(
        createZoomTransform(1, -400, -400),
        [
          [0, 0],
          [100, 100],
        ],
        [
          [0, 0],
          [200, 200],
        ]
      )
    ).toEqual({
      k: 1, // min zoom is applied
      x: -100, // scroll right
      y: -100, // scrolled down
    })
  })

  test('constrainZoomToExtent with valid transform', () => {
    expect(
      constrainZoomToExtent(
        createZoomTransform(2, -100, -100),
        [
          [0, 0],
          [100, 100],
        ],
        [
          [0, 0],
          [200, 200],
        ]
      )
    ).toEqual({
      k: 2,
      x: -100,
      y: -100,
    })
  })
  test('constrainZoomToExtent with centering tall content with invalid zoom', () => {
    expect(
      constrainZoomToExtent(
        createZoomTransform(0.5, -100, -100),
        [
          [0, 0],
          [100, 100],
        ],
        [
          [25, 0],
          [75, 100],
        ]
      )
    ).toEqual({
      k: 1,
      x: 25,
      y: 0,
    })
  })
  test('constrainZoomToExtent with centering wide content with invalid zoom', () => {
    expect(
      constrainZoomToExtent(
        createZoomTransform(0.5, -100, -100),
        [
          [0, 0],
          [100, 100],
        ],
        [
          [0, 25],
          [100, 75],
        ]
      )
    ).toEqual({
      k: 1,
      x: 0,
      y: 25,
    })
  })
  test('constrainZoomToExtent with centering wide content', () => {
    const zoom = constrainZoomToExtent(
      createZoomTransform(1.5, -100, -100),
      [
        [0, 0],
        [100, 100],
      ],
      [
        [0, 25],
        [100, 75],
      ]
    )

    expect(zoom.k).toEqual(1.5)
    expect(zoom.x).toBeCloseTo(-50)
    expect(zoom.y).toBeCloseTo(-12.5)
  })
})
