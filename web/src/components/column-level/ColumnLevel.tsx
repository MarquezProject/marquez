import * as Redux from 'redux'
import { ColumnLineageGraph } from '../../types/api'
import { CropFree, ZoomIn, ZoomOut } from '@mui/icons-material'
import { Drawer, Tooltip } from '@mui/material'
import { Graph, ZoomPanControls } from '../../../libs/graph'
import { IState } from '../../store/reducers'
import { MultipleNodeData, MultipleNodeKind, columnLevelNodeRenderer } from './nodes'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { createElkNodes, parseColumnLineageNode } from './layout'
import { fetchColumnLineage } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useCallbackRef } from '../../helpers/hooks'
import { useParams, useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import ColumnLevelDrawer from './ColumnLevelDrawer'
import IconButton from '@mui/material/IconButton'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React, { useEffect, useRef } from 'react'

interface StateProps {
  columnLineage: ColumnLineageGraph
}

interface DispatchProps {
  fetchColumnLineage: typeof fetchColumnLineage
}

type ColumnLevelProps = StateProps & DispatchProps

const zoomInFactor = 1.5
const zoomOutFactor = 1 / zoomInFactor

const ColumnLevel: React.FC<ColumnLevelProps> = ({
  fetchColumnLineage: fetchColumnLineage,
  columnLineage: columnLineage,
}: ColumnLevelProps) => {
  const { namespace, name } = useParams()
  const [searchParams, setSearchParams] = useSearchParams()

  const graphControls = useRef<ZoomPanControls>()

  useEffect(() => {
    if (name && namespace) {
      fetchColumnLineage('DATASET', namespace, name, 10)
    }
  }, [name, namespace])

  const column = searchParams.get('column')
  useEffect(() => {
    if (column) {
      console.log(`datasetField:${namespace}:${parseColumnLineageNode(column).dataset}`)
      graphControls.current?.centerOnPositionedNode(
        `datasetField:${namespace}:${parseColumnLineageNode(column).dataset}`
      )
    }
  }, [column])

  if (!columnLineage) {
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

  const { nodes, edges } = createElkNodes(columnLineage, searchParams.get('column'))

  return (
    <Box height={'calc(100vh - 98px)'}>
      <Drawer
        anchor={'right'}
        open={!!searchParams.get('dataset')}
        onClose={() => setSearchParams({})}
      >
        <Box sx={{ pt: '98px' }}>
          <ColumnLevelDrawer />
        </Box>
      </Drawer>
      <Box
        display={'flex'}
        border={1}
        borderRadius={1}
        flexDirection={'column'}
        m={1}
        position={'absolute'}
        right={0}
        zIndex={1}
        borderColor={theme.palette.grey[500]}
      >
        <Tooltip title={'Zoom in'} placement={'left'}>
          <IconButton size='small' onClick={() => handleScaleZoom('in')}>
            <ZoomIn />
          </IconButton>
        </Tooltip>
        <Tooltip title={'Zoom out'} placement={'left'}>
          <IconButton size='small' onClick={() => handleScaleZoom('out')}>
            <ZoomOut />
          </IconButton>
        </Tooltip>
        <Tooltip title={'Reset zoom'} placement={'left'}>
          <IconButton size={'small'} onClick={handleResetZoom}>
            <CropFree />
          </IconButton>
        </Tooltip>
      </Box>
      <ParentSize>
        {(parent) => (
          <Graph<MultipleNodeKind, MultipleNodeData>
            id='column-level-graph'
            backgroundColor={theme.palette.background.default}
            height={parent.height}
            width={parent.width}
            nodes={nodes}
            edges={edges}
            direction='right'
            nodeRenderers={columnLevelNodeRenderer}
            setZoomPanControls={setGraphControls}
          />
        )}
      </ParentSize>
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  columnLineage: state.columnLineage.columnLineage,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchColumnLineage: fetchColumnLineage,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(ColumnLevel)
