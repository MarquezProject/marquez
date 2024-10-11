import { CenterFocusStrong, CropFree, ZoomIn, ZoomOut } from '@mui/icons-material'
import { theme } from '../../helpers/theme'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import React from 'react'

interface ZoomControlsProps {
  handleScaleZoom: (inOrOut: 'in' | 'out') => void
  handleResetZoom: () => void
  handleCenterOnNode?: () => void
}

export const ZoomControls = ({
  handleScaleZoom,
  handleResetZoom,
  handleCenterOnNode,
}: ZoomControlsProps) => {
  return (
    <Box
      display={'flex'}
      border={1}
      borderRadius={1}
      flexDirection={'column'}
      m={1}
      position={'absolute'}
      right={0}
      zIndex={1}
      borderColor={theme.palette.grey[800]}
    >
      <MQTooltip title={'Zoom in'} placement={'left'}>
        <IconButton size='small' onClick={() => handleScaleZoom('in')}>
          <ZoomIn />
        </IconButton>
      </MQTooltip>
      <MQTooltip title={'Zoom out'} placement={'left'}>
        <IconButton size='small' onClick={() => handleScaleZoom('out')}>
          <ZoomOut />
        </IconButton>
      </MQTooltip>
      <MQTooltip title={'Reset zoom'} placement={'left'}>
        <IconButton size={'small'} onClick={handleResetZoom}>
          <CropFree />
        </IconButton>
      </MQTooltip>
      {handleCenterOnNode && (
        <MQTooltip title={'Center on selected node'} placement={'left'}>
          <IconButton size={'small'} onClick={handleCenterOnNode}>
            <CenterFocusStrong />
          </IconButton>
        </MQTooltip>
      )}
    </Box>
  )
}
