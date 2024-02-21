# graph

`graph` is a library for generating and rendering graphs. It uses elkjs to generate layouts and renders them using a custom renderer.

## Installing/Using

To use the library, add the following to your package.json:

```json
{
  "dependencies": {
    "elkjs": "^0.8.2"
  }
}
```

You also need to use the webpack CopyPlugin to copy the elk-worker file. Add the following to your webpack config:

```js
const CopyPlugin = require('copy-webpack-plugin');
const path = require('path');

// look for elkjs package folder
const elkjsRoot = path.dirname(require.resolve('elkjs/package.json'));

// add the CopyPlugin to the webpack config
  plugins: [
    ...
    new CopyPlugin({
      patterns: [
        { from: path.join(elkjsRoot, 'lib/elk-worker.min.js'), to: 'elk-worker.min.js' },
      ],
    }),
  ]
```

## useLayout

`useLayout` provides a common interface for creating layouts with [ElkJs](https://github.com/kieler/elkjs).

```ts
import { useLayout } from 'graph'

const { nodes: positionedNodes, edges: positionedEdges } = useLayout<'myKind', MyNodeDataType>({
  id,
  nodes,
  edges,
  direction: 'right',
  webWorkerUrl,
  getLayoutOptions
});
```

The layout calculations are asynchronous. Once the layout is complete, the returned `nodes` will each include
a `bottomLeftCorner: {x: number: y: number }` property, along with all the original properties.

## ZoomPanSvg Component

// To Add

## Graph Component

The Graph component is used to render a Graph. A `TaskNode` with run status is included, but custom ones are supported.

```tsx
import { Graph, Edge, Node } from 'graph';

import { MyNodeComponent, MyNodeDataType, NodeRendererMap } from './';

const myNodesRenderers: NodeRendererMap<'myNode', MyNodeDataType> = new Map().set(
  'myNode',
  MyNodeComponent,
);

// declare the nodes and edges
const nodes: Node<'myNode', MyNodeDataType>[] = [
  {
    id: '1',
    label: 'Task 1',
    type: 'myNode',
    data: {
      // any additional data you want to store
    },
  },
  {
    id: '2',
    label: 'Task 2',
    type: 'myNode',
    data: {
      // any additional data you want to store
    },
  },
];

const edges: Edge[] = [
  {
    id: '1',
    source: '1',
    target: '2',
    type: 'elbow',
  },
];

// create a graph
<Graph<'myNode', MyNodeDataType>
  nodes={nodes}
  edges={edges}
  direction="right"
  nodeRenderers={myNodesRenderers}
/>;
```
