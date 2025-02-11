// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { createTheme } from '@mui/material/styles'
import { dialogToggle } from '../store/actionCreators'
import { useTheme } from '@emotion/react'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import React, { FunctionComponent, useEffect } from 'react'
import { trackEvent } from './ga4'

interface IProps {
  dialogIsOpen: boolean
  dialogToggle: typeof dialogToggle
  ignoreWarning: () => void
  editWarningField?: string
  title?: string
}

const AlertDialog: FunctionComponent<IProps> = (props) => {
  useEffect(() => {
    if (props.dialogIsOpen) {
      trackEvent('Dialog', 'Open Dialog', props.title ?? 'No Title')
    }
  }, [props.dialogIsOpen])

  const handleClose = () => {
    props.dialogToggle('')
    trackEvent('Dialog', 'Close Dialog', props.title ?? 'No Title')
  }

  const handleContinue = () => {
    props.ignoreWarning();
    trackEvent('Dialog', 'Click Continue', props.title ?? 'No Title');
  };

  const theme = createTheme(useTheme())

  return (
    <Dialog open={props.dialogIsOpen}>
      <DialogTitle>{props.title}</DialogTitle>
      {props.editWarningField && (
        <DialogContent>
          <DialogContentText>{props.editWarningField}</DialogContentText>
        </DialogContent>
      )}
      <DialogContent>
        <DialogContentText>{props.editWarningField}</DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button
          className='dialogButton'
          color='primary'
          onClick={handleClose}
          sx={{ backgroundColor: theme.palette.error.main, color: theme.palette.common.white }}
        >
          Cancel
        </Button>
        <Button
          className='dialogButton'
          color='primary'
          variant='outlined'
          onClick={handleContinue}
        >
          Continue
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AlertDialog
