import * as Redux from 'redux'
import { ArrowBackIosRounded, CropFree, Refresh, ZoomIn, ZoomOut } from '@mui/icons-material'
import { ColumnLineageGraph } from '../../types/api'
import { Divider, Drawer, TextField, Tooltip } from '@mui/material'
import { Graph, ZoomPanControls } from '../../../libs/graph'
import { IState } from '../../store/reducers'
import { MultipleNodeData, MultipleNodeKind, columnLevelNodeRenderer } from './nodes'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { createElkNodes, parseColumnLineageNode } from './layout'
import { fetchColumnLineage } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useCallbackRef } from '../../helpers/hooks'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import ColumnLevelDrawer from './ColumnLevelDrawer'
import IconButton from '@mui/material/IconButton'
import MqText from '../core/text/MqText'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React, { useEffect, useRef, useState } from 'react'

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
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const [depth, setDepth] = useState(Number(searchParams.get('depth')) || 2)

  const graphControls = useRef<ZoomPanControls>()

  useEffect(() => {
    if (name && namespace) {
      fetchColumnLineage('DATASET', namespace, name, depth)
    }
  }, [name, namespace, depth])

  const column = searchParams.get('column')
  useEffect(() => {
    if (column) {
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

  useEffect(() => {
    setTimeout(() => {
      graphControls.current?.fitContent()
    }, 300)
  }, [nodes.length])

  return (
    <>
      <Box
        sx={{
          borderBottomWidth: 2,
          borderTopWidth: 0,
          borderLeftWidth: 0,
          borderRightWidth: 0,
          borderStyle: 'dashed',
        }}
        display={'flex'}
        height={'64px'}
        justifyContent={'space-between'}
        alignItems={'center'}
        px={2}
        borderColor={theme.palette.secondary.main}
      >
        <Box display={'flex'} alignItems={'center'}>
          <Tooltip title={'Back to datasets'}>
            <IconButton size={'small'} sx={{ mr: 2 }} onClick={() => navigate('/datasets')}>
              <ArrowBackIosRounded fontSize={'small'} />
            </IconButton>
          </Tooltip>
          <MqText heading>Datasets</MqText>
          <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
          <Box>
            <MqText subdued>Namespace</MqText>
            <MqText font={'mono'}>{namespace || 'Unknown namespace name'}</MqText>
          </Box>
          <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
          <Box>
            <MqText subdued>Name</MqText>
            <MqText font={'mono'}>{name || 'Unknown dataset name'}</MqText>
          </Box>
        </Box>
        <Box display={'flex'} alignItems={'center'}>
          <Tooltip title={'Refesh'}>
            <IconButton
              sx={{ mr: 2 }}
              color={'primary'}
              size={'small'}
              onClick={() => {
                if (namespace && name) {
                  fetchColumnLineage('DATASET', namespace, name, depth)
                }
              }}
            >
              <Refresh fontSize={'small'} />
            </IconButton>
          </Tooltip>
          <TextField
            id='column-level-depth'
            type='number'
            label='Depth'
            variant='outlined'
            size='small'
            sx={{ width: '80px' }}
            value={depth}
            onChange={(e) => {
              setDepth(isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value))
              searchParams.set('depth', e.target.value)
              setSearchParams(searchParams)
            }}
          />
        </Box>
      </Box>
      <Box height={'calc(100vh - 98px - 64px)'}>
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
    </>
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
