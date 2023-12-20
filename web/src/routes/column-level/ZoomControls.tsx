import { CropFree, ZoomIn, ZoomOut } from '@mui/icons-material'
import { Tooltip } from '@mui/material'
import { theme } from '../../helpers/theme'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import React from 'react'

interface ZoomControlsProps {
  handleScaleZoom: (inOrOut: 'in' | 'out') => void
  handleResetZoom: () => void
}

export const ZoomControls = ({ handleScaleZoom, handleResetZoom }: ZoomControlsProps) => {
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
  )
}
