// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { IState } from '../store/reducers'
import { Theme } from '@material-ui/core'
import { connect } from 'react-redux'
import { createStyles } from '@material-ui/core/styles'
import Box from '@material-ui/core/Box'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

interface IProps {
  error?: string
  success?: string
}

const styles = (theme: Theme) => {
  return createStyles({
    container: {
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
      backgroundColor: theme.palette.background.default
    },
    error: {
      color: theme.palette.error.main
    },
    success: {
      color: theme.palette.primary.main
    }
  })
}

const Toast = ({ error, success, classes }: IProps & WithStyles<typeof styles>) =>
  error || success ? (
    <Box
      className={`${classes.container} ${
        error ? classes.error : classes.success
      } shadow animated faster bounceInUp`}
    >
      <p>{error || success}</p>
    </Box>
  ) : null

const mapStateToProps = (state: IState) => ({
  error: state.display.error,
  success: state.display.success
})

export default connect(mapStateToProps)(withStyles(styles)(Toast))
