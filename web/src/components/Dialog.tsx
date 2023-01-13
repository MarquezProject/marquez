// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { dialogToggle } from '../store/actionCreators'
import { theme } from '../helpers/theme'
import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import React, { FunctionComponent } from 'react'

interface IProps {
  dialogIsOpen: boolean
  dialogToggle: typeof dialogToggle
  ignoreWarning: () => void
  editWarningField?: string
  title?: string
}

const AlertDialog: FunctionComponent<IProps> = props => {
  const handleClose = () => {
    props.dialogToggle('')
  }

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
          style={{ backgroundColor: theme.palette.error.main, color: theme.palette.common.white }}
        >
          Cancel
        </Button>
        <Button
          className='dialogButton'
          color='primary'
          variant='outlined'
          onClick={props.ignoreWarning}
        >
          Continue
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default AlertDialog
