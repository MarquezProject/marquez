// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import * as Redux from 'redux'
import { IState } from '../store/reducers'
import { Snackbar, SnackbarCloseReason } from '@mui/material'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { dialogToggle } from '../store/actionCreators'
import CloseIcon from '@mui/icons-material/Close'
import IconButton from '@mui/material/IconButton'

interface IProps {
  error?: string
  success?: string
  isOpen: boolean
}

interface IDispatchProps {
  dialogToggle: typeof dialogToggle
}

const Toast = ({ error, success, isOpen, dialogToggle }: IProps & IDispatchProps) => {
  const handleClose = (_: React.SyntheticEvent | Event, reason?: SnackbarCloseReason) => {
    if (reason === 'clickaway') {
      return
    }

    dialogToggle('error')
  }

  const action = (
    <IconButton size='small' aria-label='close' color='inherit' onClick={handleClose}>
      <CloseIcon fontSize='small' />
    </IconButton>
  )

  return (
    <Snackbar
      open={isOpen}
      autoHideDuration={5000}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      onClose={handleClose}
      message={error || success}
      action={action}
    />
  )
}

const mapStateToProps = (state: IState) => ({
  error: state.display.error,
  success: state.display.success,
  isOpen: state.display.dialogIsOpen,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      dialogToggle: dialogToggle,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Toast)
