// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { IState } from '../store/reducers'
import { connect } from 'react-redux'
import { createTheme } from '@mui/material'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'

interface IProps {
  error?: string
  success?: string
}

const Toast = ({ error, success }: IProps) => {
  const theme = createTheme(useTheme())

  return error || success ? (
    <Box
      sx={Object.assign(
        {
          position: 'fixed',
          bottom: 0,
          left: '30%',
          borderRadius: theme.shape.borderRadius,
          color: theme.palette.common.white,
          padding: theme.spacing(2),
          maxWidth: '40%',
          minWidth: '40%',
          textAlign: 'center',
          border: `2px dashed ${theme.palette.secondary.main}`,
          borderBottom: 'none',
          backgroundColor: theme.palette.background.default,
        },
        error ? { color: theme.palette.error.main } : { color: theme.palette.primary.main }
      )}
      className={'shadow animated faster bounceInUp'}
    >
      <p>{error || success}</p>
    </Box>
  ) : null
}

const mapStateToProps = (state: IState) => ({
  error: state.display.error,
  success: state.display.success,
})

export default connect(mapStateToProps)(Toast)
