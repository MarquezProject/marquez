/* GENERAL */
import type { PropsWithChildren, ReactElement } from 'react'

import { ElkLabel } from 'elkjs'

// a point in 2d space
export interface Point {
  x: number
  y: number
}

export type Direction = 'up' | 'down' | 'left' | 'right'

/* EDGES */

export interface Edge {
  id: string
  type?: string
  color?: string
  strokeWidth?: number
  isAnimated?: boolean
  sourceNodeId: string
  targetNodeId: string
  label?: string
}

export interface PositionedEdge extends Omit<Edge, 'label'> {
  startPoint: Point
  bendPoints?: Point[]
  endPoint: Point
  container: string
  label?: ElkLabel
}

/* GENERIC NODES */

export interface Node<K, D> {
  id: string
  kind: K
  width?: number
  height?: number
  padding?: { left: number; top: number; right: number; bottom: number }
  color?: string
  children?: Node<K, D>[]
  data: D
}

export type PositionedNode<K, D> = {
  id: string
  kind: K
  bottomLeftCorner: Point
  width: number
  height: number
  color?: string
  children?: PositionedNode<K, D>[]
  data: D
}

/* GRAPH NODES */

interface NodeRendererProps<K, D> extends PropsWithChildren {
  node: PositionedNode<K, D>
  isMiniMap?: boolean
}

export interface NodeRenderer<K, D> {
  (props: NodeRendererProps<K, D>): ReactElement<any, any> | null
  getLayoutOptions: (node: Node<K, D>) => Node<K, D>
}

export type NodeRendererMap<K, D> = Map<K, NodeRenderer<K, D>>

export type GraphTaskNodeStatus =
  | 'scheduled'
  | 'queued'
  | 'connecting'
  | 'running'
  | 'success'
  | 'warning'
  | 'error'
  | 'cancelled'

export interface GraphTaskNodeData {
  name: string
  operator?: string
  onClick?: (e: React.MouseEvent<HTMLElement>) => void
  onToggleCollapse?: (id: string) => void
  isSelected?: boolean
  status?: GraphTaskNodeStatus
  executionStartTime?: number | string
  executionEndTime?: number | string
  childrenCount?: number // inserted by getLayoutOptions
  collapsed?: boolean
  operatorIcon?: React.ReactNode
  mappedCount?: number
}
