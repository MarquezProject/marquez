import * as Redux from 'redux'
import { ActionBar } from './ActionBar'
import { Box, CircularProgress } from '@mui/material'
import { DEFAULT_MAX_SCALE, Graph, ZoomPanControls } from '../../../libs/graph'
import { Drawer } from '@mui/material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { JobOrDataset } from '../../types/lineage'
import { LineageGraph } from '../../types/api'
import { TableLevelNodeData, tableLevelNodeRenderer } from './nodes'
import { ZoomControls } from '../column-level/ZoomControls'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { createElkNodes } from './layout'
import { fetchLineage } from '../../store/actionCreators'
import { trackEvent } from '../../components/ga4'
import { useCallbackRef } from '../../helpers/hooks'
import { useParams, useSearchParams } from 'react-router-dom'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React, { useEffect, useRef, useState } from 'react'
import TableLevelDrawer from './TableLevelDrawer'

interface StateProps {
  lineage: LineageGraph
  isLoading: boolean
}

interface DispatchProps {
  fetchLineage: typeof fetchLineage
}

type TableLevelProps = StateProps & DispatchProps

const zoomInFactor = 1.5
const zoomOutFactor = 1 / zoomInFactor

const TableLevel: React.FC<TableLevelProps> = ({
  fetchLineage: fetchLineage,
  lineage: lineage,
  isLoading: isLoading,
}: TableLevelProps) => {
  const { nodeType, namespace, name } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()

  const [depth, setDepth] = useState(Number(searchParams.get('depth')) || 2)
  const [isCompact, setIsCompact] = useState(searchParams.get('isCompact') === 'true')
  const [isFull, setIsFull] = useState(searchParams.get('isFull') === 'true')

  const graphControls = useRef<ZoomPanControls>()

  const collapsedNodes = searchParams.get('collapsedNodes')

  useEffect(() => {
    if (name && namespace && nodeType) {
      fetchLineage(nodeType as JobOrDataset, namespace, name, depth, true)
    }
  }, [name, namespace, depth])

  useEffect(() => {
    trackEvent('TableLevel', 'View Table-Level Lineage')
  }, [])

  const handleScaleZoom = (inOrOut: 'in' | 'out') => {
    graphControls.current?.scaleZoom(inOrOut === 'in' ? zoomInFactor : zoomOutFactor)
    trackEvent('TableLevel', `Zoom ${inOrOut === 'in' ? 'In' : 'Out'}`)
  }

  const handleResetZoom = () => {
    graphControls.current?.fitContent()
    trackEvent('TableLevel', 'Reset Zoom')
  }

  const handleCenterOnNode = () => {
    graphControls.current?.centerOnPositionedNode(
      `${nodeType}:${namespace}:${name}`,
      DEFAULT_MAX_SCALE
    )
    trackEvent('TableLevel', 'Center on Node', `${nodeType}:${namespace}:${name}`)
  }

  const setGraphControls = useCallbackRef((zoomControls) => {
    graphControls.current = zoomControls
  })

  const { nodes, edges } = createElkNodes(
    lineage,
    `${nodeType}:${namespace}:${name}`,
    isCompact,
    isFull,
    collapsedNodes
  )

  useEffect(() => {
    setTimeout(() => {
      graphControls.current?.fitContent()
    }, 300)
  }, [nodes.length, isCompact])

  if (isLoading) {
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
          isLoading={isLoading}
        />
        <Box
          display='flex'
          justifyContent='center'
          alignItems='center'
          height={`calc(100vh - ${HEADER_HEIGHT}px - 64px)`}
          sx={{ bgcolor: 'secondy.main' }}
        >
          <CircularProgress />
        </Box>
      </>
    )
  }

  if (!lineage) {
    return <div />
  }

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
        isLoading={isLoading}
      />
      <Box height={`calc(100vh - ${HEADER_HEIGHT}px - ${HEADER_HEIGHT}px - 1px)`}>
        <Drawer
          anchor={'right'}
          open={!!searchParams.get('tableLevelNode')}
          onClose={() => setSearchParams({})}
          PaperProps={{
            sx: {
              backgroundColor: theme.palette.background.default,
              backgroundImage: 'none',
              mt: `${HEADER_HEIGHT}px`,
              height: `calc(100vh - ${HEADER_HEIGHT}px)`,
            },
          }}
        >
          <Box>
            <TableLevelDrawer />
          </Box>
        </Drawer>
        <ZoomControls
          handleCenterOnNode={handleCenterOnNode}
          handleScaleZoom={handleScaleZoom}
          handleResetZoom={handleResetZoom}
        />
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
  isLoading: state.lineage.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchLineage: fetchLineage,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(TableLevel)
