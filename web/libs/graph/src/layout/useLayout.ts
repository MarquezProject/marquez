import { useEffect, useMemo, useRef, useState } from 'react'

import ELK, { ElkNode } from 'elkjs'
import isEqual from 'lodash/isEqual'

import { useCallbackRef } from '../utils/hooks'
import type { Direction, Edge, Node, NodeRenderer, PositionedEdge, PositionedNode } from '../types'

export interface Props<K, D> {
  id?: string
  nodes: Node<K, D>[]
  edges: Edge[]
  direction?: Direction
  keepPreviousGraph?: boolean
  webWorkerUrl?: string
  getLayoutOptions?: NodeRenderer<K, D>['getLayoutOptions']
}

interface Output<K, D> {
  layout?: {
    nodes: PositionedNode<K, D>[]
    edges: PositionedEdge[]
    height: number
    width: number
  }
  error?: any
  isRendering: boolean
}

const positionNodes = <K, D>(nodes: Node<K, D>[], elkOutput: ElkNode[]) =>
  nodes.reduce<PositionedNode<K, D>[]>((acc, node) => {
    const elkNode = elkOutput?.find((child) => child.id === node.id)
    if (!elkNode) return acc
    const { x, y, height, width } = elkNode
    if (!x || !y || !height || !width) return acc

    acc.push({
      ...node,
      height,
      width,
      bottomLeftCorner: {
        x,
        y,
      },
      children:
        node.children && elkNode.children
          ? positionNodes<K, D>(node.children, elkNode.children)
          : undefined,
    })
    return acc
  }, [])

const addLayoutOptions = <K, D>(
  nodes: Node<K, D>[],
  getLayoutOptions: (node: Node<K, D>) => Node<K, D>
): Node<K, D>[] =>
  nodes.map((n) => {
    const newNode = getLayoutOptions(n) // getLayoutOptions() may remove children
    return {
      ...newNode,
      children: newNode.children ? addLayoutOptions(newNode.children, getLayoutOptions) : undefined,
    }
  })

export const useLayout = <K, D>({
  id: rootId = 'root',
  nodes: nodesWithoutRenderOptions,
  edges,
  direction = 'right',
  keepPreviousGraph: keepPreviousLayout = false,
  webWorkerUrl = '/elk-worker.min.js',
  getLayoutOptions = (node) => node,
}: Props<K, D>): Output<K, D> => {
  /* STATE */
  // Layout is stored in a ref to support `keepPreviousGraph`.
  const layoutRef = useRef<Output<K, D>['layout']>()

  // The graph we want rendered in the layout.
  const elkInputRef = useRef<ElkNode>()

  // After the elk async layout completes, it stores the root node.
  const [elkOutput, setElkOutput] = useState<ElkNode>()
  const [error, setError] = useState<any>()

  // The graph that was last rendered by elk. Used to determine when rendering is complete.
  const [elkRenderedInput, setElkRenderedInput] = useState<ElkNode>()

  // Assuming that if getLayoutOptions changes, it shouldn't cause a render. This creates static dependency for useMemo.
  const getLayoutOptionsRef = useCallbackRef(getLayoutOptions)

  const nodes = useMemo(
    () => addLayoutOptions(nodesWithoutRenderOptions, getLayoutOptionsRef),
    [nodesWithoutRenderOptions, getLayoutOptionsRef]
  )

  /* ELK INPUT (GRAPH) UPDATES */
  /* update the elkInput graph or keep the version last stored on the ref
   * nodes may include may data state changes that do not affect layout.
   */
  const elkInput = useMemo(() => {
    const layoutOptions = {
      'elk.interactiveLayout': 'false',
      'elk.algorithm': 'layered',
      'elk.separateConnectedComponents': 'false',
      'elk.direction': direction.toUpperCase(),
      'org.eclipse.elk.layered.nodePlacement.bk.edgeStraightening': 'NONE',
      'org.eclipse.elk.layered.edgeRouting.splines.mode': 'SLOPPY',
      'cycleBreaking.strategy': 'INTERACTIVE',
      'elk.layered.nodePlacement.strategy': 'STRETCH_WIDTH',
      'portAlignment.default': 'CENTER',
      'layered.layering.strategy': 'COFFMAN_GRAHAM',
      'layered.crossingMinimization.strategy': 'LAYER_SWEEP',
      'nodeSize.options': 'SPACE_EFFICIENT_PORT_LABELS',
      'layered.mergeEdges': 'false',
      contentAlignment: 'V_CENTER',
      'crossingMinimization.semiInteractive': 'false',
      hierarchyHandling: 'INCLUDE_CHILDREN',
      'nodeLabels.placement': '[H_CENTER, V_TOP, INSIDE]',
    }

    const mapNode = ({ id, width, height, padding, children }: Node<K, D>): ElkNode => ({
      id,
      width: width ?? 0,
      height: height ?? 0,
      children: children ? children.map(mapNode) : undefined,
      layoutOptions: padding
        ? {
            'elk.padding': `[left=${padding.left}, top=${padding.top}, right=${padding.right}, bottom=${padding.bottom}]`,
          }
        : undefined,
    })
    const newElkInput = {
      id: rootId,
      layoutOptions,
      children: nodes.map(mapNode),
      edges: edges.map((edge) => ({
        id: edge.id,
        sources: [edge.sourceNodeId],
        targets: [edge.targetNodeId],
        labels: edge.label
          ? [
              {
                id: edge.label,
                text: edge.label,
                height: 20,
                width: edge.label.length * 7,
              },
            ]
          : [],
      })),
    }

    // If the graph hasn't changed, don't update the object to prevent useEffect triggering.
    if (elkInputRef.current && isEqual(newElkInput, elkInputRef.current)) {
      return elkInputRef.current
    }

    // If the graph has changed store it on the ref and return the new value.
    elkInputRef.current = newElkInput
    return newElkInput
  }, [rootId, nodes, edges, direction])

  /* EFFECTS */
  // Render
  useEffect(() => {
    const elk = new ELK({
      workerUrl: webWorkerUrl,
    })

    elk
      .layout(elkInput)
      .then((rootNode) => {
        setElkRenderedInput(elkInput)
        setElkOutput(rootNode)
        setError(undefined)
      })
      .catch((err) => {
        setElkRenderedInput(elkInput)
        setElkOutput(undefined)
        setError(err)
      })

    // Cancel the current job when input changes.
    return () => {
      // @ts-expect-error https://github.com/kieler/elkjs/issues/208
      if (elk.worker) elk.terminateWorker()
    }
  }, [elkInput, webWorkerUrl])

  /* RETURN VALUES */

  // If the `elkInput` matches the `elkRenderedInput`, then rendering is complete.
  const isRendering = !(elkOutput || error) || elkRenderedInput !== elkInput

  // combined the nodes and edge inputs with the elk output
  const layout = useMemo(() => {
    // if elk is updating, return the last layout value or undefined
    if (isRendering) return keepPreviousLayout ? layoutRef.current : undefined

    // if there are no child nodes, there is no layout
    if (!elkOutput?.children)
      return {
        nodes: [],
        edges: [],
        height: elkOutput?.height || 0,
        width: elkOutput?.width || 0,
      }

    const newNodes = positionNodes(nodes, elkOutput.children)

    const newEdges = edges.reduce<PositionedEdge[]>((acc, edge) => {
      const elkEdge = elkOutput?.edges?.find((e) => e.id === edge.id)
      if (!elkEdge?.sections?.[0]) return acc
      const section = elkEdge.sections[0]

      acc.push({
        ...edge,
        // @ts-expect-error container is an undocumented property, but it's necessary for render.
        container: elkEdge.container,
        startPoint: section.startPoint,
        bendPoints: section.bendPoints,
        endPoint: section.endPoint,
        // set default isAnimated to true
        isAnimated: true,
        label: elkEdge?.labels?.length ? elkEdge.labels[0] : undefined,
      })
      return acc
    }, [])

    const memoLayout = {
      nodes: newNodes,
      edges: newEdges,
      height: elkOutput.height || 0,
      width: elkOutput.width || 0,
    }

    layoutRef.current = memoLayout

    return memoLayout
  }, [isRendering, elkOutput, keepPreviousLayout, edges, nodes])

  return { layout, error: isRendering ? undefined : error, isRendering }
}
