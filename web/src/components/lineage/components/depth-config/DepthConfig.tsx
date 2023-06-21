// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, Typography } from '@material-ui/core'
import { Theme, WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { setLineageGraphDepth } from '../../../../store/actionCreators'
import React from 'react'
import TextField from '@material-ui/core/TextField'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      position: 'absolute',
      display: 'flex',
      justifyContent: 'space-evenly',
      alignItems: 'center',
      right: 0,
      marginRight: '3rem',
      padding: '1rem',
      zIndex: theme.zIndex.appBar
    },
    title: {
      textAlign: 'center'
    },
    textField: {
      width: '4rem',
      marginLeft: '0.5rem'
    }
  })

interface DepthConfigProps extends WithStyles<typeof styles> {
  depth: number
  setDepth: (depth: number) => void
}

const DepthConfig: React.FC<DepthConfigProps> = ({ classes, setDepth, depth }) => {
  const i18next = require('i18next')
  const GRAPH_TITLE = i18next.t('lineage.graph_depth_title')
  return (
    <Box className={classes.root}>
      <Typography>{GRAPH_TITLE}</Typography>
      <TextField
        type='number'
        value={depth}
        onChange={e => setDepth(isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value))}
        variant='outlined'
        size='small'
        aria-label={GRAPH_TITLE}
        className={classes.textField}
        inputProps={{
          min: 0,
          max: 100
        }}
      />
    </Box>
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setDepth: setLineageGraphDepth
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(withStyles(styles)(DepthConfig))
