import React from 'react'

export function useCallbackRef<T extends (...args: any[]) => any>(
  fn: T | undefined,
  deps: React.DependencyList = []
): T {
  const ref = React.useRef(fn)

  React.useLayoutEffect(() => {
    ref.current = fn
  })
  return React.useCallback(((...args) => ref.current?.(...args)) as T, deps)
}
