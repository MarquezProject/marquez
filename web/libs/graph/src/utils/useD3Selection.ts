import { useEffect, useState } from 'react'

import 'd3-transition'
import { Selection, select } from 'd3-selection'

/**
 * A hook to create a d3 selection from a React ref.
 * @param ref  - a React.RefObject
 */
export const useD3Selection = <T extends SVGElement | HTMLElement = SVGElement>(
  ref: React.RefObject<T>
) => {
  const [selection, setSelection] = useState<Selection<T, unknown, null, unknown>>()

  // After initial render `ref.current` will be immediately available for selection.
  useEffect(() => {
    if (ref.current) {
      setSelection(select(ref.current))
    }
  }, [ref])

  return selection
}
