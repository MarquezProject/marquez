import * as Redux from 'redux'
import { ActionBar } from './ActionBar'
import { Box } from '@mui/system'
import { Drawer } from '@mui/material'
import { Graph, ZoomPanControls } from '../../../libs/graph'
import { IState } from '../../store/reducers'
import { JobOrDataset } from '../../components/lineage/types'
import { LineageGraph } from '../../types/api'
import { TableLevelNodeData, tableLevelNodeRenderer } from './nodes'
import { ZoomControls } from '../column-level/ZoomControls'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { createElkNodes } from './layout'
import { fetchLineage } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useCallbackRef } from '../../helpers/hooks'
import { useParams, useSearchParams } from 'react-router-dom'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React, { useEffect, useRef, useState } from 'react'
import TableLevelDrawer from './TableLevelDrawer'

interface StateProps {
  lineage: LineageGraph
}

interface DispatchProps {
  fetchLineage: typeof fetchLineage
}

type ColumnLevelProps = StateProps & DispatchProps

const zoomInFactor = 1.5
const zoomOutFactor = 1 / zoomInFactor

const ColumnLevel: React.FC<ColumnLevelProps> = ({
  fetchLineage: fetchLineage,
  lineage: lineage,
}: ColumnLevelProps) => {
  const { nodeType, namespace, name } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()

  const [depth, setDepth] = useState(Number(searchParams.get('depth')) || 2)

  const [isCompact, setIsCompact] = useState(false)
  const [isFull, setIsFull] = useState(true)

  const graphControls = useRef<ZoomPanControls>()

  useEffect(() => {
    if (name && namespace && nodeType) {
      fetchLineage(nodeType as JobOrDataset, namespace, name, depth)
    }
  }, [name, namespace, depth])

  if (!lineage) {
    return <div />
  }

  const handleScaleZoom = (inOrOut: 'in' | 'out') => {
    graphControls.current?.scaleZoom(inOrOut === 'in' ? zoomInFactor : zoomOutFactor)
  }

  const handleResetZoom = () => {
    graphControls.current?.fitContent()
  }

  const setGraphControls = useCallbackRef((zoomControls) => {
    graphControls.current = zoomControls
  })

  const { nodes, edges } = createElkNodes(
    lineage,
    `${nodeType}:${namespace}:${name}`,
    isCompact,
    isFull
  )

  useEffect(() => {
    setTimeout(() => {
      graphControls.current?.fitContent()
    }, 300)
  }, [nodes.length, isCompact])

  return (
    <>
      <ActionBar
        nodeType={nodeType?.toUpperCase() as JobOrDataset}
        fetchLineage={fetchLineage}
        depth={depth}
        setDepth={setDepth}
        isCompact={isCompact}
        setIsCompact={setIsCompact}
        isFull={isFull}
        setIsFull={setIsFull}
      />
      <Box height={'calc(100vh - 98px - 64px)'}>
        <Drawer
          anchor={'right'}
          open={!!searchParams.get('tableLevelNode')}
          onClose={() => setSearchParams({})}
        >
          <Box sx={{ pt: '98px' }}>
            <TableLevelDrawer />
          </Box>
        </Drawer>
        <ZoomControls handleScaleZoom={handleScaleZoom} handleResetZoom={handleResetZoom} />
        <ParentSize>
          {(parent) => (
            <Graph<JobOrDataset, TableLevelNodeData>
              id='column-level-graph'
              backgroundColor={theme.palette.background.default}
              height={parent.height}
              width={parent.width}
              nodes={nodes}
              edges={edges}
              direction='right'
              nodeRenderers={tableLevelNodeRenderer}
              setZoomPanControls={setGraphControls}
            />
          )}
        </ParentSize>
      </Box>
    </>
  )
}

const mapStateToProps = (state: IState) => ({
  lineage: state.lineage.lineage,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchLineage: fetchLineage,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(ColumnLevel)
