// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Snackbar } from '@mui/material'
import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import IconButton from '@mui/material/IconButton'
import MQTooltip from '../tooltip/MQTooltip'
import React from 'react'

interface MqCopyProps {
  string: string
}

const MqEmpty: React.FC<MqCopyProps> = ({ string }) => {
  const [open, setOpen] = React.useState(false)
  const handleClose = (event: React.SyntheticEvent | Event, reason?: string) => {
    if (reason === 'clickaway') {
      return
    }

    setOpen(false)
  }
  return (
    <>
      <MQTooltip title='Copy'>
        <IconButton
          onClick={(event) => {
            event.stopPropagation()
            navigator.clipboard.writeText(string)
            setOpen(true)
          }}
          aria-label='copy'
          size={'small'}
          color={'primary'}
        >
          <ContentCopyIcon fontSize={'small'} />
        </IconButton>
      </MQTooltip>
      <Snackbar
        open={open}
        autoHideDuration={2000}
        onClose={handleClose}
        message={`Copied ${string}`}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      />
    </>
  )
}

export default MqEmpty
