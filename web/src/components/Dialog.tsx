// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { dialogToggle } from '../store/actionCreators'
import { theme } from '../helpers/theme'
import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'

interface IProps {
  dialogIsOpen: boolean
  dialogToggle: typeof dialogToggle
  ignoreWarning: () => void
  editWarningField?: string
  title?: string
}

export default function AlertDialog(props: IProps) {
  function handleClose() {
    props.dialogToggle('')
  }

  return (
    <div>
      <Dialog open={props.dialogIsOpen}>
        <DialogTitle>{props.title}</DialogTitle>
        {props.editWarningField &&
          <DialogContent>
            <DialogContentText>{props.editWarningField}</DialogContentText>
          </DialogContent>
        }
        <DialogContent>
          <DialogContentText>{props.editWarningField}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button
            className='dialogButton'
            color='primary'
            onClick={handleClose}
            style={{ backgroundColor: theme.palette.error.main, color: 'white' }}
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
    </div>
  )
}
