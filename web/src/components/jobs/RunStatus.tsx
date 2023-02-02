// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box, Theme, Tooltip, WithStyles, createStyles, withStyles } from '@material-ui/core'
import { Run } from '../../types/api'
import { runStateColor } from '../../helpers/nodes'

import React, { FunctionComponent } from 'react'

const styles = (theme: Theme) => {
  return createStyles({
    status: {
      width: theme.spacing(2),
      height: theme.spacing(2),
      borderRadius: '50%'
    }
  })
}

interface RunStatusProps {
  run: Run
}

const RunStatus: FunctionComponent<RunStatusProps & WithStyles<typeof styles>> = props => {
  const { run, classes } = props
  return (
    <Tooltip title={run.state}>
      <Box
        mr={1}
        className={classes.status}
        style={{ backgroundColor: runStateColor(run.state) }}
      />
    </Tooltip>
  )
}

export default withStyles(styles)(RunStatus)
